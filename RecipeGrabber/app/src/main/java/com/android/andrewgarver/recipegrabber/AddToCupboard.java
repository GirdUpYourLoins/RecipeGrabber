package com.android.andrewgarver.recipegrabber;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;

/**
 * Class to add ingredients to the Cupboard
 * <p>
 * Creates fields to add quantity, units, and ingredients up to 20 at a time
 *   to your cupboard.
 *
 * @author  Andrew Garver, Landon Jamieson, and Reed Atwood
 * @version 1.0
 * @since   12/10/2015
 */
public class AddToCupboard extends AppCompatActivity {

    /**
     * Debugging Tag to display LogCat messages for debugging
     */
    private static final String TAG = AddToCupboard.class.getSimpleName();

    private DatabaseAdapter dbHelper;

    /**
     * Flag to ensure all entries are filled
     */
    private boolean correctInput = true;

    /**
     * Keep track of how many new lines there is
     */
    private int numNewLines;

    /**
     * Array of ids for each of he rows to keep track of what is on them
     */
    private int ids[] = {R.id.newRow1, R.id.newRow2, R.id.newRow3, R.id.newRow4, R.id.newRow5,
            R.id.newRow6, R.id.newRow7, R.id.newRow8, R.id.newRow9, R.id.newRow10,
            R.id.newRow11, R.id.newRow12, R.id.newRow13, R.id.newRow14, R.id.newRow15,
            R.id.newRow16, R.id.newRow17, R.id.newRow18, R.id.newRow19, R.id.newRow20};

    /**
     * Opens AddToCupboard Activity so that you can add a ingredients to the cupboard
     * <p>
     * Adds fields to add quantity, units, and ingredients to the database.
     * Sets numNewLines to 0.
     *
     * @param savedInstanceState save the activity for reopening
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_cupboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        numNewLines = 0;
        Log.i(TAG, "Started Add To Cupboard");
        dbHelper = new DatabaseAdapter(this);

        /**
         * this must be final since it is accessed from an inner class
         */
        final ImageButton add = (ImageButton) findViewById(R.id.addMore);
        final Button addIng = (Button) findViewById(R.id.addIng);
        final ArrayList<Ingredient> results = new ArrayList<>();

        /**
         * need to add the listener to add an extra row of input fields
         */
        add.setOnClickListener(new View.OnClickListener() {

            /**
             * When they click the + button, they will get another row for input.
             * <p>
             * Insures that there in no more than 20 new lines, When the plus(+)
             *   button is clicked, we add a new row to the View and increments
             *   numNewLines by 1.
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                /**
                 * Don't let the user enter any more than 20 new lines
                 */
                if (numNewLines > 19)
                    return;

                /**
                 * We use the context of the button, since it is on the activity we are using
                 */
                LayoutInflater vi = (LayoutInflater) add.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View v = vi.inflate(R.layout.input_field, null); //This is the layout of the new row

                /**
                 * update id of container and then increment number of new lines
                 */
                v.setId(ids[numNewLines]);
                ++numNewLines;

                /**
                 * There is an (at first) empty container LinearLayout that we insert these into
                 */
                ((ViewGroup) findViewById(R.id.container)).addView(v);

                Log.i(TAG, "added line " + numNewLines + " with id of " + ids[numNewLines - 1]);
            }
        });

        addIng.setOnClickListener(new View.OnClickListener() {

            /**
             * Adds ingredient to the database and broadcasts the changes
             * <p>
             * Adds ingredient's quantity, units, and ingredient to the database
             * Checks to ensure that all of the fields are filled out.
             * Clicking the add button adds the first row to the database
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                Log.i(TAG, "adding to cupboard");

                /**
                 * adds the first row to database
                 * Quantity = quant
                 * Ingredient Name = ingName
                 */
                String quant = ((EditText)findViewById(R.id.ingQuant)).getText().toString();
                String unit = ((Spinner)findViewById(R.id.ingUnit)).getSelectedItem().toString();
                String ingName = ((EditText)findViewById(R.id.ingName)).getText().toString();
                ingName = ingName.replace("  ", " "); //remove double spaces if any

                /**
                 * Error handling for if quantity and ingredient name are blank
                 */
                if (!quant.equals("") && !ingName.equals("")) {

                    if (dbHelper.addIngredient(quant, unit, ingName) > 0) {
                        Log.i(TAG, "added ingredients to cupboard");
                        results.add(new Ingredient(ingName, Integer.parseInt(quant), unit));
                        
                        // reset the flag
                        correctInput = true;

                    } else {
                        Log.i(TAG, "failed to add ingredient");
                        correctInput = false;
                    }
                } else {
                    correctInput = false;
                }

                /**
                 * Adds any additional rows.
                 */
                for (int i = 0; i < numNewLines; ++i) {
                    RelativeLayout rel = ((RelativeLayout)findViewById(ids[i]));
                    quant = ((EditText)rel.findViewById(R.id.quanNewRow)).getText().toString();
                    unit = ((Spinner)rel.findViewById(R.id.unitNewRow)).getSelectedItem().toString();
                    ingName = ((EditText)rel.findViewById(R.id.nameNewRow)).getText().toString();
                    ingName = ingName.replace("  ", " "); //remove double spaces if any

                    /**
                     * Add more ingredients if there is more than one.
                     */
                    if (!quant.equals("") && !ingName.equals("")) {
                        if (dbHelper.addIngredient(quant, unit, ingName) > 0) {
                            Log.i(TAG, "added to cupboard");
                            results.add(new Ingredient(ingName, Integer.parseInt(quant), unit));
                        } else {
                            Log.i(TAG, "failed to add ingredients");
                        }
                    }

                    Log.i(TAG, "added line " + i + 1 + " to DB with id of " + ids[i]);
                }

                /**
                 * Update the activity if all fields are filled out correctly and displays a
                 *   message if not all filled out
                 */
                if (correctInput) {
                    Intent data = new Intent();
                    data.putExtra("results", results);
                    setResult(RESULT_OK, data);
                    finish(); // This takes us back to the previous fragment
                } else {
                    Toast.makeText(getApplicationContext(), "Please fill out all fields",
                             Toast.LENGTH_LONG).show();
                }
                
            }
        });
    }
}