package com.example.chatify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private ImageButton optionsMenuIcon;

    private FrameLayout fragmentcontainer;
    private TextInputLayout searchLayout;
    private TextInputEditText search_edittext;
    private RecyclerView recyclerView;
    private ConversationAdapter adapter;
    private List<Conversation> conversationList;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton drawerIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        fragmentcontainer = findViewById(R.id.fragment_container);
        drawerIcon = findViewById(R.id.drawericon);
        optionsMenuIcon = findViewById(R.id.menuIcon);
        recyclerView = findViewById(R.id.conversation_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        searchLayout = findViewById(R.id.search_bar_layout);
        search_edittext = findViewById(R.id.search_edittext);

        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        conversationList = new ArrayList<>();
        adapter = new ConversationAdapter(this, conversationList);
        recyclerView.setAdapter(adapter);

        drawerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        optionsMenuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsMenu();
            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                fragmentcontainer.setVisibility(View.VISIBLE);
                if (itemId == R.id.nav_profile) {
                    openFragment(new ProfileFragment());
                }  else if (itemId == R.id.nav_help) {
                    openFragment(new HelpFragment());
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        setupSearchListener();   loadConversationsForCurrentUser();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;  }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getApplicationContext(), "Signed out", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void setupSearchListener() {
        search_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {


            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    Toast.makeText(getApplicationContext(),"here",Toast.LENGTH_SHORT).show();
                    String searchText = search_edittext.getText().toString().trim();
                    if (!searchText.isEmpty()) {
                        searchUserAndStartConversation(searchText);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void searchUserAndStartConversation(String searchText) {
        firestore.collection("users")
                .whereEqualTo("userName", searchText)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userId = document.getString("userId");
                            String email = document.getString("email");
                            String displayName = document.getString("displayName");
                            String username = document.getString("userName");
                            User user = new User(userId, email, displayName, username);
                            createConversation(user);
                            return; // Assuming username is unique, we can exit after the first match
                        }
                        Toast.makeText(MainActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Error searching for user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void createConversation(User selectedUser) {
        String currentUserUid = currentUser.getUid();
        String selectedUserId = selectedUser.getUserId();

        // Create conversation IDs
        String conversationId1 = currentUserUid + "_" + selectedUserId;
        String conversationId2 = selectedUserId + "_" + currentUserUid;

        // Check if conversationId1 exists
        firestore.collection("conversations")
                .document(conversationId1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            // Check if conversationId2 exists
                            firestore.collection("conversations")
                                    .document(conversationId2)
                                    .get()
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            if (!task2.getResult().exists()) {
                                                // Both conversations do not exist, create new conversation
                                                Conversation newConversation = new Conversation(currentUserUid, selectedUserId, null);

                                                // Store the conversation in Firestore under conversationId1
                                                firestore.collection("conversations")
                                                        .document(conversationId1)
                                                        .set(newConversation)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Toast.makeText(MainActivity.this, "Conversation started with " + selectedUser.getUserName(), Toast.LENGTH_SHORT).show();
                                                            conversationList.add(newConversation);
                                                            adapter.notifyDataSetChanged();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(MainActivity.this, "Failed to create conversation", Toast.LENGTH_SHORT).show();
                                                        });
                                            } else {
                                                Toast.makeText(MainActivity.this, "Conversation already exists with " + selectedUser.getUserName(), Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(MainActivity.this, "Error checking conversation: " + task2.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(MainActivity.this, "Conversation already exists with " + selectedUser.getUserName(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Error checking conversation: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void openFragment(Fragment fragment) {
        recyclerView.setVisibility(View.GONE);
        searchLayout.setVisibility(View.GONE);
        fragmentcontainer.setVisibility(View.VISIBLE);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void showOptionsMenu() {
        PopupMenu popupMenu = new PopupMenu(this, optionsMenuIcon);
        popupMenu.inflate(R.menu.options_menu);

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.logout) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getApplicationContext(), "Signed out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }



    private void loadConversationsForCurrentUser() {
        String currentUserId = currentUser.getUid();

        // Query for conversations where current user is involved (either as user1 or user2)
        firestore.collection("conversations")
                .whereEqualTo("user1_id", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Conversation conversation = document.toObject(Conversation.class);
                        conversationList.add(conversation);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to load conversations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        firestore.collection("conversations")
                .whereEqualTo("user2_id", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Conversation conversation = document.toObject(Conversation.class);
                        conversationList.add(conversation);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to load conversations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void onBackPressed() {
        if (fragmentcontainer.getVisibility() == View.VISIBLE) {

            fragmentcontainer.setVisibility(View.GONE);
            searchLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);

        } else {
            super.onBackPressed();
        }
    }

}
