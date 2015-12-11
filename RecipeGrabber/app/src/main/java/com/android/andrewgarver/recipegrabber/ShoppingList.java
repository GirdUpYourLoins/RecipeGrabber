package com.android.andrewgarver.recipegrabber;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.android.andrewgarver.recipegrabber.extendCalView.CalendarProvider;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Andrew Garver on 11/2/2015.
 */
public class ShoppingList extends Fragment {

    private final static int shoppingCode = 3;
    private final static String TAG = ShoppingList.class.getSimpleName();
    private DatabaseAdapter dbHelper;
    private ListView list;
    private ArrayAdapter<String> adapter;
    CalendarProvider cpHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dbHelper = new DatabaseAdapter(getActivity());
        cpHelper = new CalendarProvider();
        cpHelper.setContext(getActivity());

        // This is where we automatically add things to the shopping list

        // We need to refresh the shoppingList before anything so we don't get duplicates
        dbHelper.refreshShoppingList();

        // Gets the recipe names
        ArrayList<String> plannedRecipes = cpHelper.getPlannedRecipes();
        // Get the recipe id numbers and store them in an ArrayList
        ArrayList<Integer> plannedRecipeIds = new ArrayList<>();
        for (String s : plannedRecipes) {
            plannedRecipeIds.add(dbHelper.getRecipeId(s));
        }
        // Get the ingredients from the recipes using the recipe_ids
        final ArrayList<Ingredient> plannedIngredients = dbHelper.getPlannedIngredients(plannedRecipeIds);

        // Get the ingredients in the Cupboard
        ArrayList<Ingredient> cupboardIngredients = dbHelper.getAllIngredientsVerbose();

        // Get the already planned items on the shopping list
        ArrayList<Ingredient> shoppingListItems = dbHelper.getAllShoppingListItemsVerbose();

        // Do math with ingredients
        for (Ingredient planned : plannedIngredients) {
            boolean haveInCupboard = false;
            for (Ingredient cupboard : cupboardIngredients) {
                if (planned.getName().toLowerCase().equals(cupboard.getName().toLowerCase()) // the same ingredient
                    && planned.getMetric().equals(cupboard.getMetric())) { // with the same metric unit
                    haveInCupboard = true;
                            if (planned.getQuantity() > cupboard.getQuantity()) { // don't have enough in the cupboard
                        // Add the results to the shopping list
                        dbHelper.addToShoppingList(planned.getName(), String.valueOf(planned.getQuantity() - cupboard.getQuantity()), planned.getMetric(), true);
                    }
                }
            }
            if (!haveInCupboard) {
                dbHelper.addToShoppingList(planned.getName(), String.valueOf(planned.getQuantity()), planned.getMetric(), true);
            }
        }

        // This just gets all the items in the shopping list.  This includes manually added as well as automatically added things.
        ArrayList<String> items = dbHelper.getAllShoppingListItems(); // this is what we need to update (In another function)

        adapter = new ArrayAdapter<>(getContext(), R.layout.row_layout, items);

        View view = inflater.inflate(R.layout.frag_shoppinglist, container, false);
        list = (ListView) view.findViewById(R.id.listView);
        list.setAdapter(adapter);

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final String preSplit = adapter.getItem(position);
                final String toDel = preSplit.split(" - ")[0];
                AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
                adb.setTitle("Delete ingredient: " + toDel + '?');
                adb.setMessage("Are you sure you want to remove this ingredient from your shopping list?");
                adb.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (Ingredient ing : plannedIngredients)
                            if (ing.getName().equals(toDel)) {
                                Toast.makeText(getContext(), "Unable to delete planned ingredient\n" +
                                        "Remove recipe from the Menu or add ingredient to the cupboard", Toast.LENGTH_LONG).show();
                                return;
                            }

                        adapter.remove(preSplit);
                        dbHelper.deleteFromShoppingList(toDel);
                        Toast.makeText(getContext(), "Deleting from shopping list", Toast.LENGTH_LONG).show();
                    }
                });
                adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog ad = adb.create();
                ad.show();
                return true;
            }
        });

        ImageButton addBtn = (ImageButton) view.findViewById(R.id.addToShoppingList);
        addBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(new Intent(getContext(), AddToShoppingList.class), shoppingCode);
            }
        });

        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == shoppingCode) {
            if (resultCode == getActivity().RESULT_OK) {
                ArrayList<String> shopping = data.getStringArrayListExtra("results");
                adapter.addAll(shopping);
            }
        }
    }
}
