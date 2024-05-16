package uk.ac.cam.cares.jps.timeline.ui.manager;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mapbox.bindgen.Expected;
import com.mapbox.bindgen.None;
import com.mapbox.bindgen.Value;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.LayerPosition;
import com.mapbox.maps.MapView;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import uk.ac.cam.cares.jps.timeline.viewmodel.TrajectoryViewModel;

public class TrajectoryManager {
    private TrajectoryViewModel trajectoryViewModel;
    private Logger LOGGER = Logger.getLogger(TrajectoryManager.class);

    public TrajectoryManager(Fragment fragment, MapView mapView) {
        trajectoryViewModel = new ViewModelProvider(fragment).get(TrajectoryViewModel.class);

        trajectoryViewModel.trajectory.observe(fragment.getViewLifecycleOwner(), trajectory -> {
            mapView.getMapboxMap().getStyle(style -> {
                Expected<String, None> removeLayerSuccess = style.removeStyleLayer("trajectory_layer");
                Expected<String, None> removeSourceSuccess = style.removeStyleSource("trajectory");
                LOGGER.debug("trajectory: layer and source removed result " + (removeSourceSuccess.isError() && removeLayerSuccess.isError() ? removeSourceSuccess.getError() + "\n" + removeLayerSuccess.getError() : "success"));

                if (trajectory.isEmpty()) {
                    return;
                }

                JSONObject sourceJson = new JSONObject();
                try {
                    sourceJson.put("type", "geojson");
                    sourceJson.put("data", trajectory);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                Expected<String, None> success = style.addStyleSource("trajectory", Objects.requireNonNull(Value.fromJson(sourceJson.toString()).getValue()));
                LOGGER.debug("trajectory: source created " + (success.isError() ? success.getError() : "success"));

                JSONObject layerJson = new JSONObject();
                try {
                    layerJson.put("id", "trajectory_layer");
                    layerJson.put("type", "line");
                    layerJson.put("source", "trajectory");
                    layerJson.put("line-join", "bevel");

                    JSONObject paint = new JSONObject();
                    paint.put("line-color", "#00687C");
                    paint.put("line-width", 8);
                    layerJson.put("paint", paint);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                Expected<String, None> layerSuccess = style.addStyleLayer(Objects.requireNonNull(Value.fromJson(layerJson.toString()).getValue()), new LayerPosition(null, null, null));
                LOGGER.debug("trajectory: layer created " + (layerSuccess.isError() ? layerSuccess.getError() : "success"));
            });

            try {
                JSONObject jsonObject = new JSONObject(trajectory);
                JSONArray bbox = jsonObject.getJSONArray("bbox");
                mapView.getMapboxMap().cameraAnimationsPlugin(plugin -> {
                    Point newCenter = getBBoxCenter(bbox);
                    if (newCenter == null) {
                        return null;
                    }

                    plugin.flyTo(new CameraOptions.Builder()
                            .center(getBBoxCenter(bbox))
                            .build(),
                            new MapAnimationOptions.Builder().duration(2000).build(),
                            null);
                    return null;
                });
            } catch (JSONException e) {
                LOGGER.info("No trajectory retrieved, no need to reset camera");
            }

        });
    }

    private Point getBBoxCenter(JSONArray bbox) {
        try {
            double lngAvg = (bbox.getDouble(0) + bbox.getDouble(2)) / 2;
            double latAvg = (bbox.getDouble(1) + bbox.getDouble(3)) / 2;
            return Point.fromLngLat(lngAvg, latAvg);
        } catch (JSONException e) {
            return null;
        }
    }
}
