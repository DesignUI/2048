package com.if3games.game2048;

import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;

public class MainActivity extends BaseGameActivity implements InputListener.Listener, MainGame.Listener {

	MainView view;
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String SCORE = "score";
	public static final String HIGH_SCORE = "high score temp";
	public static final String UNDO_SCORE = "undo score";
	public static final String CAN_UNDO = "can undo";
	public static final String UNDO_GRID = "undo";
	public static final String GAME_STATE = "game state";
	public static final String UNDO_GAME_STATE = "undo game state";
	
    // request codes we use when invoking an external activity
    final int RC_RESOLVE = 5000, RC_UNUSED = 5001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		view = new MainView(this);

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		view.hasSaveState = settings.getBoolean("save_state", false);

		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean("hasState")) {
				load();
			}
		}
		setContentView(view);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			// Do nothing
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			view.game.move(2);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			view.game.move(0);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			view.game.move(3);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			view.game.move(1);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBoolean("hasState", true);
		save();
	}

	protected void onPause() {
		super.onPause();
		save();
	}

	private void save() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		Tile[][] field = view.game.grid.field;
		Tile[][] undoField = view.game.grid.undoField;
		editor.putInt(WIDTH, field.length);
		editor.putInt(HEIGHT, field.length);
		for (int xx = 0; xx < field.length; xx++) {
			for (int yy = 0; yy < field[0].length; yy++) {
				if (field[xx][yy] != null) {
					editor.putInt(xx + " " + yy, field[xx][yy].getValue());
				} else {
					editor.putInt(xx + " " + yy, 0);
				}

				if (undoField[xx][yy] != null) {
					editor.putInt(UNDO_GRID + xx + " " + yy,
							undoField[xx][yy].getValue());
				} else {
					editor.putInt(UNDO_GRID + xx + " " + yy, 0);
				}
			}
		}
		editor.putLong(SCORE, view.game.score);
		editor.putLong(HIGH_SCORE, view.game.highScore);
		editor.putLong(UNDO_SCORE, view.game.lastScore);
		editor.putBoolean(CAN_UNDO, view.game.canUndo);
		editor.putInt(GAME_STATE, view.game.gameState);
		editor.putInt(UNDO_GAME_STATE, view.game.lastGameState);
		editor.commit();
	}

	protected void onResume() {
		super.onResume();
		load();
	}

	private void load() {
		// Stopping all animations
		view.game.aGrid.cancelAnimations();

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		for (int xx = 0; xx < view.game.grid.field.length; xx++) {
			for (int yy = 0; yy < view.game.grid.field[0].length; yy++) {
				int value = settings.getInt(xx + " " + yy, -1);
				if (value > 0) {
					view.game.grid.field[xx][yy] = new Tile(xx, yy, value);
				} else if (value == 0) {
					view.game.grid.field[xx][yy] = null;
				}

				int undoValue = settings.getInt(UNDO_GRID + xx + " " + yy, -1);
				if (undoValue > 0) {
					view.game.grid.undoField[xx][yy] = new Tile(xx, yy,
							undoValue);
				} else if (value == 0) {
					view.game.grid.undoField[xx][yy] = null;
				}
			}
		}

		view.game.score = settings.getLong(SCORE, view.game.score);
		view.game.highScore = settings.getLong(HIGH_SCORE, view.game.highScore);
		view.game.lastScore = settings.getLong(UNDO_SCORE, view.game.lastScore);
		view.game.canUndo = settings.getBoolean(CAN_UNDO, view.game.canUndo);
		view.game.gameState = settings.getInt(GAME_STATE, view.game.gameState);
		view.game.lastGameState = settings.getInt(UNDO_GAME_STATE,
				view.game.lastGameState);
	}

	@Override
	public void onSignInFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSignInSucceeded() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onShowAchievementsRequested() {
	    if (isSignedIn()) {
	        startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()),
	                    RC_UNUSED);
	    } else {
	        // start the sign-in flow
	        beginUserInitiatedSignIn();
	    }
	}

	@Override
	public void onShowLeaderboardsRequested() {
	    if (isSignedIn()) {
	    	startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(getApiClient()),
	                RC_UNUSED);
	    } else {
	        // start the sign-in flow
	        beginUserInitiatedSignIn();
	    }	
	}
	
    final static int[] ACHIEVEMENT = {
        R.string.achievement_32, R.string.achievement_64, R.string.achievement_128, 
        R.string.achievement_256, R.string.achievement_512,R.string.achievement_1024, 
        R.string.achievement_2048, R.string.achievement_4096
    };
    
    final static int[] ACHIEVEMENT_BLOCKS = { 32, 64, 128, 256, 512, 1024, 2048, 4096 };

	@Override
	public void onUnlockAchievement(int mergedValue) {
    	for (int i = 0; i < ACHIEVEMENT_BLOCKS.length; i++) {
			if(mergedValue == ACHIEVEMENT_BLOCKS[i]) {
				Games.Achievements.unlock(getApiClient(), getString(ACHIEVEMENT[i]));
				break;
			}
		}		
	}

	@Override
	public void onStoreScoreLeaderboard(long highScore) {
		Games.Leaderboards.submitScore(getApiClient(), getString(R.string.leaderboard_score), highScore);
	}
}
