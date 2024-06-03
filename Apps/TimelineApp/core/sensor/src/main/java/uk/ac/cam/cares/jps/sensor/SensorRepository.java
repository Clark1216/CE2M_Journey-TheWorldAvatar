package uk.ac.cam.cares.jps.sensor;

import android.content.Context;
import android.content.Intent;

import org.apache.log4j.Logger;

import uk.ac.cam.cares.jps.utils.RepositoryCallback;
import uk.ac.cam.cares.jps.login.LoginRepository;

public class SensorRepository {
    UserPhoneNetworkSource userPhoneNetworkSource;
    SensorCollectionStateManagerRepository sensorCollectionStateManagerRepository;
    Logger LOGGER = Logger.getLogger(SensorRepository.class);
    Intent serviceIntent;
    Context context;

    public SensorRepository(Context applicationContext,
                            UserPhoneNetworkSource userPhoneNetworkSource,
                            SensorCollectionStateManagerRepository sensorCollectionStateManagerRepository) {
        this.context = applicationContext;
        this.userPhoneNetworkSource = userPhoneNetworkSource;
        this.sensorCollectionStateManagerRepository = sensorCollectionStateManagerRepository;

        serviceIntent = new Intent(context, SensorService.class);
    }

    public void startRecording(RepositoryCallback<Boolean> callback) {
        LOGGER.info("start recording");
        sensorCollectionStateManagerRepository.getDeviceId(new RepositoryCallback<>() {
            @Override
            public void onSuccess(String result) {
                serviceIntent.putExtra("deviceId", result);
                context.startService(serviceIntent);
                sensorCollectionStateManagerRepository.setRecordingState(true);
                callback.onSuccess(true);
            }

            @Override
            public void onFailure(Throwable error) {
                callback.onFailure(error);
            }
        });


    }

    public void stopRecording() {
        LOGGER.info("stop recording");
        context.stopService(serviceIntent);
        sensorCollectionStateManagerRepository.setRecordingState(false);
    }
}
