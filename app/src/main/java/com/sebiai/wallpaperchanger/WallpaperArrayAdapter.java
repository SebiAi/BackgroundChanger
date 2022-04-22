package com.sebiai.wallpaperchanger;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.fragment.app.Fragment;

import com.sebiai.wallpaperchanger.objects.Wallpaper;

import java.util.ArrayList;

public class WallpaperArrayAdapter extends ArrayAdapter<Wallpaper> {
    private final Context context;
    private final int layoutResourceId;
    ArrayList<Wallpaper> wallpapers;
    private final Fragment fragment;

    public WallpaperArrayAdapter(Context context, int layoutResourceId, ArrayList<Wallpaper> wallpapers, Fragment fragment) {
        super(context, layoutResourceId, wallpapers);

        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.wallpapers = wallpapers;
        this.fragment = fragment;
    }

    @Override
    public int getCount() {
        return wallpapers.size();
    }

    // Holds the views in the current row
    private static class ViewHolder {

    }


    // TODO: Use this instead: https://developer.android.com/guide/topics/ui/layout/cardview, https://developer.android.com/guide/topics/ui/layout/recyclerview, https://developer.android.com/reference/kotlin/androidx/recyclerview/widget/GridLayoutManager
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ViewHolder holder;
//
//        if (convertView == null) {
//            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
//            convertView = inflater.inflate(layoutResourceId, parent, false);
//            String ingredientName = ingredientParts.get(position).getName();
//
//            holder = new ViewHolder();
//
//            // Ingredient Name
//            holder.ingredientName = convertView.findViewById(R.id.text_listrow_ingredientname);
//            holder.ingredientName.setText(ingredientName);
//
//            // Amount ml
//            holder.amountMl = convertView.findViewById(R.id.text_listrow_ml);
//            holder.amountMl.setText(String.format(fragment.getString(R.string.text_amount_sum_ml_string), ingredientParts.get(position).milliliters));
//
//            // Close "button"
//            holder.imageViewClose = convertView.findViewById(R.id.imageview_listrow_close);
//            holder.imageViewClose.setOnClickListener(v -> {
//                // Ask user if he wants to delete the Ingredient
//                QuestionDialogFragment dialog = new QuestionDialogFragment("Zutat löschen",
//                        "Möchtest du wirklich den Eintrag '" + ingredientName + "' löschen?", "LÖSCHEN", "ABBRECHEN",
//                        fragment, position);
//                dialog.show(fragment.requireActivity().getSupportFragmentManager(), fragment.getString(R.string.TAG_DELETE_QUESTION));
//            });
//            holder.imageViewClose.setColorFilter(holder.ingredientName.getCurrentTextColor());
//
//            // Up "button"
//            holder.imageViewUp = convertView.findViewById(R.id.imageview_listrow_up);
//            holder.imageViewUp.setColorFilter(holder.ingredientName.getCurrentTextColor());
//            holder.imageViewUp.setOnClickListener(v -> {
//                if (position == 0 || ingredientParts.size() <= 1)
//                    return;
//                IngredientPart part = ingredientParts.get(position);
//                ingredientParts.add(position - 1, part);
//                ingredientParts.remove(position + 1);
//                notifyDataSetChanged();
//            });
//
//            // Down "button"
//            holder.imageViewDown = convertView.findViewById(R.id.imageview_listrow_down);
//            holder.imageViewDown.setColorFilter(holder.ingredientName.getCurrentTextColor());
//            holder.imageViewDown.setOnClickListener(v -> {
//                if (position == ingredientParts.size() - 1 || ingredientParts.size() <= 1)
//                    return;
//                IngredientPart part = ingredientParts.get(position);
//                ingredientParts.add(position + 2, part);
//                ingredientParts.remove(position);
//                notifyDataSetChanged();
//            });
//
//            convertView.setTag(holder);
//        } else {
//            ((TextView)convertView.findViewById(R.id.text_listrow_ingredientname)).setText(ingredientParts.get(position).getName());
//            ((TextView)convertView.findViewById(R.id.text_listrow_ml)).setText(String.format(fragment.getString(R.string.text_amount_sum_ml_string), ingredientParts.get(position).milliliters));
//        }
//
//
//        return convertView;
//    }
}
