package pt.up.fc.dcc.mousetrap.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import pt.up.fc.dcc.mousetrap.R;
import pt.up.fc.dcc.mousetrap.models.Trap;
import pt.up.fc.dcc.mousetrap.models.TrapImage;
import pt.up.fc.dcc.mousetrap.utils.TrapPicasso;

/**
 * Adapter for traps list
 *
 * @author José C. Paiva <up201200272@fc.up.pt>
 */
public class TrapsListAdapter extends ArrayAdapter<Trap> {

    public TrapsListAdapter(@NonNull Context context) {
        super(context, 0);
        Trap t = new Trap("x15nvnurbj7");
        t.addImage(new TrapImage("58f8f70e977c7a001f9a3bfc"));
        add(t);
        add(new Trap("x15nvnurbjj"));
        add(new Trap("x15nvnurbj4"));
        add(new Trap("x15nvnud7jt"));
        add(new Trap("x15nvnuijf5"));
        add(new Trap("x15nvnurbj9"));
        add(new Trap("x15nvnug08c"));
        add(new Trap("x15nvnuaaa7"));
        add(new Trap("x15nvnyqgj3"));
        add(new Trap("x15nvnurbju"));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup viewGroup) {
        ImageView picture;
        TextView name;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_traps_item, viewGroup, false);
            view.setTag(R.id.picture, view.findViewById(R.id.picture));
            view.setTag(R.id.name, view.findViewById(R.id.name));
        }

        picture = (ImageView) view.getTag(R.id.picture);
        name = (TextView) view.getTag(R.id.name);

        Trap trap = getItem(position);
        assert trap != null;

        if (trap.getImageUrl() == null)
            picture.setImageResource(R.drawable.placeholder);
        else
            TrapPicasso.getPicasso(getContext())
                    .load(trap.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(picture);

        name.setText(trap.getName());

        return view;
    }
}
