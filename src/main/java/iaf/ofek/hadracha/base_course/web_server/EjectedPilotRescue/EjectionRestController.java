package iaf.ofek.hadracha.base_course.web_server.EjectedPilotRescue;

import iaf.ofek.hadracha.base_course.web_server.Data.InMemoryMapDataBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ejectedPilotRescue")
public class EjectionRestController {

  InMemoryMapDataBase dataBase;
  AirplanesAllocationManager airplanesAllocationManager;

  public EjectionRestController(@Autowired InMemoryMapDataBase dataBase, @Autowired AirplanesAllocationManager airplanesAllocationManager) {
    this.airplanesAllocationManager = airplanesAllocationManager;
    this.dataBase = dataBase;
  }

  @GetMapping("/infos")
  public List<EjectedPilotInfo> SendEjectionToClient() {
    return dataBase.getAllOfType(EjectedPilotInfo.class);
  }

  @GetMapping("/takeResponsibility")
  public void TakeResponsibility(
      @RequestParam int ejectionId,
      @CookieValue(value = "client-id", defaultValue = "") String clientId) {
    EjectedPilotInfo ejectedPilotInfo = dataBase.getByID(ejectionId, EjectedPilotInfo.class);
    if (ejectedPilotInfo.rescuedBy == null) {
      ejectedPilotInfo.rescuedBy = clientId;
    }
    dataBase.update(ejectedPilotInfo);
    airplanesAllocationManager.allocateAirplanesForEjection(ejectedPilotInfo, clientId);
  }
}
