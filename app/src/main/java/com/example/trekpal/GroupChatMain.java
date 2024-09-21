package com.example.trekpal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GroupChatMain extends AppCompatActivity {

    private ArrayList<String> chatMessages = new ArrayList<>();
    private ArrayList<String> customMessages = new ArrayList<>();
    private ChatAdapter chatAdapter;
    private static final int MAX_CUSTOM_MESSAGES = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_main);

        // Retrieve activity details from intent
        String activityName = getIntent().getStringExtra("activityName");
        String activityType = getIntent().getStringExtra("activityType");
        String activityDate = getIntent().getStringExtra("activityDate");

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(activityName);  // Set the title of the toolbar
        }


        // Set up the RecyclerView
        RecyclerView chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setAdapter(chatAdapter);


        // Predefined default messages
        String[] defaultMessages = {
                "Hello, I've Reached the Destination",
                "Need to Stop at the Nearest Stopping Point!"
        };

        // Display default messages as buttons
        for (String message : defaultMessages) {
            addMessageButton(message);
        }

        ImageButton addMessageButton = findViewById(R.id.addMessageButton);
        addMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customMessages.size() < MAX_CUSTOM_MESSAGES) {
                    showAddMessageDialog();
                } else {
                    showLimitReachedDialog();
                }
            }
        });
    }

    // Inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);  // Inflate the toolbar menu
        return true;
    }

    // Handle toolbar menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuGroupDetails) {
            // Handle Open Screen 1 (functionality not added yet)
            return true;
        } else if (id == R.id.menuAddMembers) {
            // Navigate to AddMemberScreen
            Intent intent = new Intent(GroupChatMain.this, AddMemberScreen.class);
            startActivity(intent);

            return true;
        } else if (id == R.id.action_open_screen3) {
            // Handle Open Screen 3 (functionality not added yet)
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void showAddMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Custom Message");

        final EditText input = new EditText(this);
        input.setHint("Enter your message here");
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String customMessage = input.getText().toString();
                if (!customMessage.isEmpty()) {
                    customMessages.add(customMessage);
                    addMessageButton(customMessage);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void showLimitReachedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Limit Reached")
                .setMessage("You can only add up to " + MAX_CUSTOM_MESSAGES + " custom messages.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void addMessageButton(final String message) {
        final Button button = new Button(this);
        button.setText(message);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatMessages.add(message);
                chatAdapter.notifyDataSetChanged();  // Refresh RecyclerView
            }
        });

        // Long click listener to remove the message
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showRemoveMessageDialog(button, message);
                return true;  // Returning true to indicate the long-click action was handled
            }
        });

        // Add button to your LinearLayout
        LinearLayout messageContainer = findViewById(R.id.messageContainer);
        messageContainer.addView(button);
    }

    private void showRemoveMessageDialog(final Button button, final String message) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Custom Message")
                .setMessage("Do you want to remove this custom message?")
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        customMessages.remove(message);  // Remove from customMessages list
                        LinearLayout messageContainer = findViewById(R.id.messageContainer);
                        messageContainer.removeView(button);  // Remove the button from the UI
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
