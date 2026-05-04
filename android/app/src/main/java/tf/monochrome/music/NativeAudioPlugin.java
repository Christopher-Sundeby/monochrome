package tf.monochrome.music;

import android.net.Uri;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "NativeAudio")
public class NativeAudioPlugin extends Plugin {
    private ExoPlayer player;
    private boolean listenerAttached = false;

    private ExoPlayer getPlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(getContext()).build();
        }

        if (!listenerAttached) {
            player.addListener(new androidx.media3.common.Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == androidx.media3.common.Player.STATE_ENDED) {
                        JSObject data = new JSObject();
                        notifyListeners("ended", data);
                    }
                }
            });

            listenerAttached = true;
        }

        return player;
    }

    @PluginMethod
    public void play(PluginCall call) {
        String uriString = call.getString("uri");
        Double startTime = call.getDouble("startTime", 0.0);

        if (uriString == null || uriString.isEmpty()) {
            call.reject("Missing uri");
            return;
        }

        getActivity().runOnUiThread(() -> {
            try {
                ExoPlayer exoPlayer = getPlayer();

                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(uriString));

                exoPlayer.setMediaItem(mediaItem);
                exoPlayer.prepare();

                if (startTime != null && startTime > 0) {
                    exoPlayer.seekTo((long) (startTime * 1000));
                }

                exoPlayer.play();

                JSObject result = new JSObject();
                result.put("playing", true);
                result.put("uri", uriString);
                call.resolve(result);
            } catch (Exception error) {
                call.reject("Failed to play audio", error);
            }
        });
    }

    @PluginMethod
    public void pause(PluginCall call) {
        getActivity().runOnUiThread(() -> {
            if (player != null) {
                player.pause();
            }

            JSObject result = new JSObject();
            result.put("paused", true);
            call.resolve(result);
        });
    }

    @PluginMethod
    public void resume(PluginCall call) {
        getActivity().runOnUiThread(() -> {
            if (player != null) {
                player.play();
            }

            JSObject result = new JSObject();
            result.put("playing", true);
            call.resolve(result);
        });
    }

    @PluginMethod
    public void stop(PluginCall call) {
        getActivity().runOnUiThread(() -> {
            if (player != null) {
                player.stop();
                player.clearMediaItems();
            }

            JSObject result = new JSObject();
            result.put("stopped", true);
            call.resolve(result);
        });
    }

    @PluginMethod
    public void seek(PluginCall call) {
        Double position = call.getDouble("position", 0.0);

        getActivity().runOnUiThread(() -> {
            if (player != null && position != null) {
                player.seekTo((long) (position * 1000));
            }

            JSObject result = new JSObject();
            result.put("position", position);
            call.resolve(result);
        });
    }

    @PluginMethod
    public void getStatus(PluginCall call) {
        getActivity().runOnUiThread(() -> {
            JSObject result = new JSObject();

            if (player == null) {
                result.put("playing", false);
                result.put("position", 0);
                result.put("duration", 0);
                call.resolve(result);
                return;
            }

            long positionMs = Math.max(0, player.getCurrentPosition());
            long durationMs = Math.max(0, player.getDuration());

            result.put("playing", player.isPlaying());
            result.put("position", positionMs / 1000.0);
            result.put("duration", durationMs / 1000.0);

            call.resolve(result);
        });
    }

    @Override
    protected void handleOnDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }

        super.handleOnDestroy();
    }
}