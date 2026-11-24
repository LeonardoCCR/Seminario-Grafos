package grafos.model;

public class OutputBellmanFord {

    BellmanFord bellmanFord = new BellmanFord();

    public String minimumCost(AdjMatrixGraph<String> graph, String source, String destination)
    {
        bellmanFord.minimumCost(graph, source, destination);
        return null;
    }

    public String getShortestPath(AdjMatrixGraph<String> graph, String source, String destination){
        bellmanFord.getShortestPath(graph, source, destination);
         return null;
    }
}
