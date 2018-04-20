package co.yonomi.thincloud.tcsdktestplatform;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import co.yonomi.thincloud.tcsdk.thincloud.models.Command;

public class CommandListAdapter extends BaseAdapter {

    Context context;
    List<Command> data;

    private static LayoutInflater inflater = null;


    public CommandListAdapter(Context ctx, List<Command> d){
        context = ctx;
        data = d;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addCommands(List<Command> commands){
        for (Command command : commands) {
            addCommand(command, false);
        }
        notifyDataSetChanged();
    }


    public void addCommand(Command command){
        addCommand(command, true);
    }

    private void addCommand(Command command, boolean notify){
        data.add(command);
        if(notify)
            notifyDataSetChanged();
    }


    @Override
    public int getCount(){
        return data.size();
    }

    @Override
    public Object getItem(int position){
        return data.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = convertView;
        if(view == null)
            view = inflater.inflate(R.layout.row_cmd, null);

        Command command = (Command)getItem(position);

        TextView name, id;

        name = view.findViewById(R.id.name_val);
        id = view.findViewById(R.id.command_id_val);

        name.setText(command.name());
        id.setText(command.commandId());

        return view;
    }

}
