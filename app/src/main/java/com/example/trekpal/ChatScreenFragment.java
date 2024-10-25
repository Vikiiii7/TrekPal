package com.example.trekpal;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatScreenFragment extends Fragment {

    private ArrayList<ChatMessage> chatMessages = new ArrayList<>(); // Change to ChatMessage;
    private ArrayList<String> customMessages = new ArrayList<>();
    private ChatAdapter chatAdapter;
    private static final int MAX_CUSTOM_MESSAGES = 3;
    private String activityName, selectedActivityType, activityDate, uniqueCode, currentUserUniqueCode;
    private FirebaseFirestore firestore;
    private CollectionReference messagesCollection;
    private HashMap<String, String> userNamesMap = new HashMap<>();
    private RecyclerView chatRecyclerView; // Add this


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();

        // Retrieve activity details from arguments
        if (getArguments() != null) {
            activityName = getArguments().getString("activityName");
            selectedActivityType = getArguments().getString("activityType");
            activityDate = getArguments().getString("activityDate");
            uniqueCode = getArguments().getString("uniqueCode");
            currentUserUniqueCode = uniqueCode; // Ensure this is set
        }

        // Reference to the messages collection for the specific activity
        messagesCollection = firestore.collection("activityType")
                .document(selectedActivityType)
                .collection("activities")
                .document(activityName)
                .collection("messages");

        loadMessages();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_screen, container, false);

        // Set up the RecyclerView
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView); // Initialize the RecyclerView here
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatAdapter = new ChatAdapter(chatMessages, currentUserUniqueCode);
        chatRecyclerView.setAdapter(chatAdapter);

        // Predefined default messages
        String[] defaultMessages = {
                "Hello, I've Reached the Destination",
                "Need to Stop at the Nearest Stopping Point!"
        };

        // Display default messages as buttons
        LinearLayout messageContainer = view.findViewById(R.id.messageContainer);
        for (String message : defaultMessages) {
            addMessageButton(message, messageContainer);
        }

        // Get the message input EditText and send button
        EditText messageEditText = view.findViewById(R.id.messageEditText);
        ImageButton sendMessageButton = view.findViewById(R.id.sendMessageButton);


        // Handle the send button click
        sendMessageButton.setOnClickListener(v -> {
            String typedMessage = messageEditText.getText().toString().trim();
            if (!typedMessage.isEmpty()) {
                sendMessageToFirestore(typedMessage);  // Save message to Firestore
                messageEditText.setText("");  // Clear the input field after sending
            } else {
                Toast.makeText(getContext(), "Please type a message", Toast.LENGTH_SHORT).show();
            }
        });


        //for custom message addition
        ImageButton addMessageButton = view.findViewById(R.id.addMessageButton);
        addMessageButton.setOnClickListener(v -> {
            if (customMessages.size() < MAX_CUSTOM_MESSAGES) {
                showAddMessageDialog();
            } else {
                showLimitReachedDialog();
            }
        });

        return view;
    }

    private void showAddMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Custom Message");

        final EditText input = new EditText(getContext());
        input.setHint("Enter your message here");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String customMessage = input.getText().toString();
            if (!customMessage.isEmpty()) {
                customMessages.add(customMessage);
                addMessageButton(customMessage, getView().findViewById(R.id.messageContainer));
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showLimitReachedDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Limit Reached")
                .setMessage("You can only add up to " + MAX_CUSTOM_MESSAGES + " custom messages.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void addMessageButton(final String message, LinearLayout messageContainer) {
        final Button button = new Button(getContext());
        button.setText(message);
        button.setOnClickListener(v -> {
            sendMessageToFirestore(message);  // Save message to Firestore
        });

        // Long click listener to remove the message
        button.setOnLongClickListener(v -> {
            showRemoveMessageDialog(button, message, messageContainer);
            return true;  // Returning true to indicate the long-click action was handled
        });

        // Add button to your LinearLayout
        messageContainer.addView(button);
    }

    private void showRemoveMessageDialog(final Button button, final String message, LinearLayout messageContainer) {
        new AlertDialog.Builder(getContext())
                .setTitle("Remove Custom Message")
                .setMessage("Do you want to remove this custom message?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    customMessages.remove(message);  // Remove from customMessages list
                    messageContainer.removeView(button);  // Remove the button from the UI
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendMessageToFirestore(String messageText) {
        Map<String, Object> message = new HashMap<>();
        message.put("messageText", messageText);
        message.put("senderUniqueCode", uniqueCode);
        message.put("timestamp", com.google.firebase.Timestamp.now());

        messagesCollection.add(message)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Chat", "Message sent: " + messageText);
                })
                .addOnFailureListener(e -> {
                    Log.w("Chat", "Error sending message", e);
                    Toast.makeText(getContext(), "Failed to send message", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadMessages() {
        messagesCollection.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("Chat", "Listen failed.", e);
                        return;
                    }

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Map<String, Object> messageData = dc.getDocument().getData();
                            String messageText = (String) messageData.get("messageText");
                            String senderUniqueCode = (String) messageData.get("senderUniqueCode");
                            com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) messageData.get("timestamp");

                            // Format the timestamp to AM/PM
                            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                            String formattedTime = sdf.format(timestamp.toDate());

                            // Fetch username if not cached
                            if (!userNamesMap.containsKey(senderUniqueCode)) {
                                loadUsername(senderUniqueCode, messageText, formattedTime);
                            } else {
                                // Username is cached, directly add message with username and time
                                String username = userNamesMap.get(senderUniqueCode);
                                chatMessages.add(new ChatMessage(username + ": " + messageText, formattedTime, senderUniqueCode)); // Add ChatMessage object
                                chatAdapter.notifyDataSetChanged();

                                // Scroll to the latest message
                                scrollToBottom();
                            }
                        }
                    }
                });
    }

    private void loadUsername(String uniqueCode, String messageText, String formattedTime) {
        firestore.collection("newUsers")
                .document(uniqueCode)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        userNamesMap.put(uniqueCode, username);
                        chatMessages.add(new ChatMessage(username + ": " + messageText, formattedTime, uniqueCode)); // Add ChatMessage object
                        ; // Add ChatMessage object
                        chatAdapter.notifyDataSetChanged();

                        // Scroll to the latest message
                        scrollToBottom();
                    }
                })
                .addOnFailureListener(e -> Log.w("Chat", "Error loading username", e));
    }


    // Helper method to scroll to the bottom of the chat
    private void scrollToBottom() {
        if (chatRecyclerView != null && chatAdapter.getItemCount() > 0) {
            chatRecyclerView.scrollToPosition(chatMessages.size() - 1); // Safely reference the RecyclerView
        }
    }

}