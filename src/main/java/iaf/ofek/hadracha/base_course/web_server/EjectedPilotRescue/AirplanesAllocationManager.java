package iaf.ofek.hadracha.base_course.web_server.EjectedPilotRescue;

import iaf.ofek.hadracha.base_course.web_server.AirSituation.AirSituationProvider;
import iaf.ofek.hadracha.base_course.web_server.AirSituation.Airplane;
import iaf.ofek.hadracha.base_course.web_server.AirSituation.AirplaneKind;
import iaf.ofek.hadracha.base_course.web_server.Data.Coordinates;
import iaf.ofek.hadracha.base_course.web_server.Utilities.GeographicCalculations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AirplanesAllocationManager {
    private final GeographicCalculations geographicCalculations;
    private final AirSituationProvider airSituationProvider;

    public AirplanesAllocationManager(AirSituationProvider airSituationProvider, GeographicCalculations geographicCalculations) {
        this.airSituationProvider = airSituationProvider;
        this.geographicCalculations = geographicCalculations;
    }

    /**
     * Finds the airplanes that should be allocated to an ejected-pilot-rescue mission
     * @param ejectedPilotInfo the mission details
     * @param controllerClientId client ID of the (human) controller that is responsible for the mission, to be filled
     *                           on the airplane.
     */
    void allocateAirplanesForEjection(EjectedPilotInfo ejectedPilotInfo, String controllerClientId){

        // The current algorithm - find the closest available airplane of each kind

        List<Airplane> airplanes = airSituationProvider.getAllAirplanes();
        Coordinates ejectionCoordinates = ejectedPilotInfo.getCoordinates();

        Map<AirplaneKind, ClosestAirplane> closestAirplanes = new HashMap<>();
        closestAirplanes.put(AirplaneKind.Krav, new ClosestAirplane());
        closestAirplanes.put(AirplaneKind.Katmam, new ClosestAirplane());
        closestAirplanes.put(AirplaneKind.Maskar, new ClosestAirplane());
        closestAirplanes.put(AirplaneKind.Masaar, new ClosestAirplane());

        airplanes.stream()
                .filter(airplane -> !airplane.isAllocated())
                .forEach(airplane -> {
                    closestAirplanes.keySet().forEach(kind -> {
                        if (airplane.getAirplaneKind().isDescendantOf(kind))
                            closestAirplanes.get(kind).updateIfCloser(airplane, ejectionCoordinates);
                    });
                });

        closestAirplanes.values().forEach(closestAirplane -> {
            if (closestAirplane.closestAirplane!=null) {
                ejectedPilotInfo.allocateAirplane(closestAirplane.closestAirplane, controllerClientId);
            }
        });
    }

    private class ClosestAirplane {
        private Airplane closestAirplane;
        private double closestDistance;

        public void updateIfCloser(Airplane airplane, Coordinates closeTo) {
            double distance = geographicCalculations.distanceBetween(airplane.coordinates, closeTo);

            if (thisIsTheFirstAirplane() || closestDistance > distance) {
                closestDistance = distance;
                closestAirplane = airplane;
            }
        }

        private boolean thisIsTheFirstAirplane() {
            return closestAirplane == null;
        }
    }
}
