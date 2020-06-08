package snow.player.state;

import android.content.Context;

import org.junit.Test;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.runner.RunWith;

import snow.player.Player;
import snow.player.playlist.PlaylistPlayer;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PersistentPlaylistStateTest {

    @Test
    public void constructorTest() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String id = "test_persistent_playlist";

        PersistentPlaylistState pps = new PersistentPlaylistState(context, id);

        pps.setPlayProgress(100);
        pps.setSoundQuality(Player.SoundQuality.HIGH);
        pps.setAudioEffectEnabled(true);
        pps.setOnlyWifiNetwork(false);
        pps.setIgnoreLossAudioFocus(false);
        pps.setPosition(50);
        pps.setPlayMode(PlaylistPlayer.PlayMode.SHUFFLE);

        PersistentPlaylistState pps2 = new PersistentPlaylistState(context, id);

        assertEquals(pps, pps2);
    }
}