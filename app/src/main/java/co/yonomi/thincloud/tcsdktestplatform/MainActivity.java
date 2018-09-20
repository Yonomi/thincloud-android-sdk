package co.yonomi.thincloud.tcsdktestplatform;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import co.yonomi.thincloud.tcsdk.ThincloudConfig;
import co.yonomi.thincloud.tcsdk.ThincloudSDK;
import co.yonomi.thincloud.tcsdk.cq.CommandHandler;
import co.yonomi.thincloud.tcsdk.thincloud.APISpec;
import co.yonomi.thincloud.tcsdk.thincloud.ThincloudAPI;
import co.yonomi.thincloud.tcsdk.thincloud.ThincloudRequest;
import co.yonomi.thincloud.tcsdk.thincloud.ThincloudResponse;
import co.yonomi.thincloud.tcsdk.thincloud.exceptions.ThincloudException;
import co.yonomi.thincloud.tcsdk.thincloud.models.AccessToken;
import co.yonomi.thincloud.tcsdk.thincloud.models.BaseResponse;
import co.yonomi.thincloud.tcsdk.thincloud.models.Command;
import co.yonomi.thincloud.tcsdk.thincloud.models.Device;
import co.yonomi.thincloud.tcsdk.thincloud.models.User;
import co.yonomi.thincloud.tcsdktestplatform.example.lights.models.State;
import java9.util.concurrent.CompletableFuture;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Gson gson = new Gson();

        final Configurator appConfig = Configurator.initWithContext(this);

        final Spinner envSelector = findViewById(R.id.environment);
        final EditText textUsername = findViewById(R.id.username);
        final EditText textPassword = findViewById(R.id.password);
        final Button buttonLogin = findViewById(R.id.button_login);
        final Button buttonCreateCmd = findViewById(R.id.button_create_command);
        final Button buttonLogout = findViewById(R.id.button_logout);
        final Button buttonFWrite = findViewById(R.id.button_fwrite);
        final Button buttonFRead = findViewById(R.id.button_fread);
        final ListView commandList = findViewById(R.id.command_list);
        final TextView textUserId = findViewById(R.id.user_id);
        final TextView textClientId = findViewById(R.id.client_id);

        final CommandListAdapter commandListAdapter = new CommandListAdapter(this, new ArrayList<>());

        commandList.setAdapter(commandListAdapter);


        textUsername.setText(appConfig.get("thincloud.defaultTestUser", ""));
        textPassword.setText(appConfig.get("thincloud.defaultTestPass", ""));

        // Configure environment spinner
        ArrayAdapter<CharSequence> envArrayAdapter = ArrayAdapter.createFromResource(this, R.array.env_array, android.R.layout.simple_spinner_item);
        envArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        envSelector.setAdapter(envArrayAdapter);


        ThincloudConfig config = new ThincloudConfig()
                .appName(getAppName())
                .appVersion(getAppVersion())
                .apiKey(appConfig.get("thincloud.apiKey"))
                .clientId(appConfig.get("thincloud.clientId"))
                .instanceName(appConfig.get("thincloud.instanceName"))
                .fcmTopic(appConfig.get("thincloud.fcmTopic"));

        config.commandsToIgnore().add("_update");

        if(!config.validate())
            Toast.makeText(MainActivity.this, "Configuration is invalid", Toast.LENGTH_SHORT).show();

        if(!ThincloudSDK.isInitialized()) {
            Log.i(TAG, "SDK is not initialized");
        } else {
            Log.i(TAG, "SDK has been previously initialized");
        }
        try {
            ThincloudSDK.initialize(getApplicationContext(), config);
        }
        catch(ThincloudException e){
            Log.e(TAG, "Failed to initialize SDK", e);
        }

        final ExecCommand execCommand = new ExecCommand() {
            @Override
            Command process(Command command) {
                Command response = command.respond();
                Log.i(TAG, "Command: " + gson.toJson(command));

                try {
                    switch (command.name()) {
                        case "get_state":
                            State gotState = new State();
                            gotState
                                    .name("my light")
                                    .power(true)
                                    .brightness(50)
                                    .saturation(50)
                                    .hue(0);
                            response.response(
                                    new Command.Response()
                                            .result((JsonObject) gson.toJsonTree(gotState))
                            );
                            break;
                        case "update_state":
                            State updateState = gson.fromJson(command.request(), State.class);
                            updateState.brightness(25);
                            response.response(
                                    new Command.Response()
                                            .result((JsonObject) gson.toJsonTree(updateState))
                            );
                            break;

                        case "delta_state":
                            State deltaState = new State().brightness(50).temperature(2500);
                            response.response(new Command.Response().result((JsonObject) gson.toJsonTree(deltaState)));
                            break;

                        case "batch_command":
                            Command[] commands = gson.fromJson(command.request().get("commands"), Command[].class);
                            for (int i = 0; i < commands.length; i++) {
                                commands[i] = process(commands[i]);
                            }
                            JsonObject result = new JsonObject();
                            result.add("commands", gson.toJsonTree(commands));
                            response.response(new Command.Response().result(result));
                            break;
                    }
                } catch(Exception e){
                    Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }

                response.state("completed");

                return response;
            }
        };

        final CommandHandler commandHandler = new CommandHandler() {
            @Override
            public void onEventReceived(Command command) {
                Toast.makeText(MainActivity.this, "Processing command: " + command.commandId(), Toast.LENGTH_SHORT).show();
                Log.i("ICommandHandler", "Handling command: " + command.commandId());

                Command response = execCommand.process(command);

                onEventProcessed(response);

                commandListAdapter.addCommand(response);
            }
        };


        ThincloudSDK.setHandler(commandHandler);

        buttonCreateCmd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                APISpec apiSpec = ThincloudAPI.getInstance().getSpec();
                if(apiSpec != null){
                    new ThincloudRequest<List<Device>>().create(apiSpec.getDevices(), new ThincloudResponse<List<Device>>() {
                        @Override
                        public void handle(Call<List<Device>> call, Response<List<Device>> response, Throwable error) {
                            if(error == null){
                                if(response.body() != null){
                                    if(response.body().size() > 0){
                                        Device device = response.body().get(0);
                                        Command command = new Command()
                                                .name("get_state");

                                        new ThincloudRequest<Command>().create(apiSpec.createCommand(device.deviceId(), command), new ThincloudResponse<Command>() {
                                            @Override
                                            public void handle(Call<Command> call, Response<Command> response, Throwable error) {
                                                if(error == null){
                                                    if(response.code() == 201){
                                                        Toast.makeText(MainActivity.this, "Created command", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Log.e(TAG, "Failed to create command");
                                                    }
                                                } else {
                                                    Log.e(TAG, "Failed to create command", error);
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    Log.e(TAG, "Device response was null");
                                }
                            } else {
                                Log.e(TAG, "Failed to get devices", error);
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "Failed to get API spec for create command");
                }
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
                if(getCurrentFocus() != null)
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                
                try {
                    APISpec apiSpec = ThincloudAPI.getInstance().getSpec();
                    ThincloudSDK.login(textUsername.getText().toString(), textPassword.getText().toString(), new CompletableFuture<Boolean>(){
                        @Override
                        public boolean complete(Boolean result) {
                            ThincloudResponse<User> handler = new ThincloudResponse<User>() {
                                @Override
                                public void handle(Call<User> call, Response<User> response, Throwable error) {
                                    if(error != null){
                                        Log.e(TAG, "Failed to get user", error);
                                        Toast.makeText(MainActivity.this, "User failed to authenticate", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if(response.code() >= 400){
                                            Log.e(TAG, "Failed to get user");
                                        } else {
                                            Log.i(TAG, "Got user " + response.body().userId());
                                            textUserId.setText(response.body().userId());
                                            try {
                                                textClientId.setText(ThincloudAPI.getInstance().getClientId());
                                            } catch(ThincloudException e){
                                                Log.e(TAG, "Failed to get client id post login", e);
                                            }
                                            Toast.makeText(MainActivity.this, "User authenticated", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            };
                            new ThincloudRequest<User>().create(apiSpec.getSelf(), handler);
                            return true;
                        }

                        @Override
                        public boolean completeExceptionally(Throwable e){
                            Log.e(TAG, "Failed to login", e);
                            return true;
                        }
                    });
                } catch(ThincloudException e){
                    Log.e(TAG, "Failed to initialize SDK", e);
                    Toast.makeText(MainActivity.this, "SDK Init Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ThincloudSDK.logout();
                    Toast.makeText(MainActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();
                } catch(ThincloudException e){
                    Log.e(TAG, "Failed to logout", e);
                    Toast.makeText(MainActivity.this, "Logout Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonFWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                APISpec apiSpec = ThincloudAPI.getInstance().getSpec();

                if(apiSpec != null){
                    Map<String, String> hashMap = new HashMap<>();
                    hashMap.put("hello", "world");
                    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), gson.toJson(hashMap));
                    new ThincloudRequest<BaseResponse>().create(apiSpec.putSelfFile("testFile", requestBody), new ThincloudResponse<BaseResponse>() {
                        @Override
                        public void handle(Call<BaseResponse> call, Response<BaseResponse> response, Throwable error) {
                            if(response.code() == 201)
                                Log.i(TAG, "Put File: Success");
                            else
                                Log.e(TAG, "Put File: Failed");
                        }
                    });
                }
            }
        });

        buttonFRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                APISpec apiSpec = ThincloudAPI.getInstance().getSpec();

                if(apiSpec != null){
                    new ThincloudRequest<ResponseBody>().create(apiSpec.getSelfFile("testFile"), new ThincloudResponse<ResponseBody>() {
                        @Override
                        public void handle(Call<ResponseBody> call, Response<ResponseBody> response, Throwable error) {
                            if (error == null) {
                                if (response.code() == 200)
                                    try {
                                        Log.i(TAG, "Got File: " + response.body().string());
                                    }
                                    catch(IOException e){
                                        Log.e(TAG, "Failed to get file: " + e.getMessage());
                                    }
                                else
                                    Log.e(TAG, "Failed to get file: Request error");
                            } else {
                                Log.e(TAG, "Failed to get file: " + error.getMessage());
                            }
                        }
                    });
                }
            }
        });
    }

    private String getAppName(){
//        ApplicationInfo appInfo = getApplicationContext().getApplicationInfo();
//        return getApplicationContext().getString(appInfo.labelRes);
        return getApplicationContext().getPackageName();
    }

    private String getAppVersion(){
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch(PackageManager.NameNotFoundException e){
            Log.e(TAG, "Failed to get app version", e);
            return "VersionNotFound";
        }
    }

    private abstract class ExecCommand {
        abstract Command process(Command command);
    }
}
