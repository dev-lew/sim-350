package hw3;

import java.util.HashMap;
import java.util.Set;

class Router {
    /*
      The map describes with what probability a given
      request exiting the server will go elsewhere
      The server will have to provide its own request timeline

      Integer represents server name
    */
    private HashMap<Server, Double> routingMap =
        new HashMap<>();

    public void addRoute(Server s, double p) {
        this.routingMap.put(s, p);
    }

    public HashMap<Server, Double> getRoutingMap() {
        return routingMap;
    }

    public Set<Server> getDestinationServers() {
        return this.routingMap.keySet();
    }

    public Double getProb(Server s) {
        return this.routingMap.get(s);
    }
}
