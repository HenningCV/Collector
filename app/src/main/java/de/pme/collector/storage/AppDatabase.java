package de.pme.collector.storage;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.Collections;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.pme.collector.model.Game;
import de.pme.collector.model.Item;


@Database(entities = {Game.class, Item.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract GameDAO gameDAO();
    public abstract ItemDAO itemDAO();

    // executor service for asynchronous operations
    private static final int NUMBER_OF_THREADS = 4;
    private static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool( NUMBER_OF_THREADS );

    // singleton
    private static volatile AppDatabase instance;


    // singleton - create and open database
    static AppDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                                    AppDatabase.class, "app_db")
                            .addCallback(createCallback)
                            .build();
                }
            }
        }
        return instance;
    }


    public static <T> T query(Callable<T> task)
            throws ExecutionException, InterruptedException {
        return databaseWriteExecutor.invokeAny( Collections.singletonList(task) );
    }

    public static void execute(Runnable runnable) {
        databaseWriteExecutor.execute(runnable);
    }


    // initial data
    private static final RoomDatabase.Callback createCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            execute(() -> {
                GameDAO gameDAO = instance.gameDAO();
                gameDAO.deleteAll();

                ItemDAO itemDAO = instance.itemDAO();
                itemDAO.deleteAll();

                // create new dummy-games
                for (int i = 0; i < 8; i++) {
                    Game game = new Game("Game " + i, "Publisher Game " + i, String.valueOf(i));
                    gameDAO.insert(game);

                    // create dummy-items for each game
                    for (int j = 0; j < (5+i); j++) {
                        Item item = new Item(gameDAO.getLastEntry().getId(), "", "Item " + i, "Description of Item " + i, "Prerequisites of Item " + i, "Location of Item " + i);
                        itemDAO.insert(item);
                    }
                }
            });
        }
    };
}