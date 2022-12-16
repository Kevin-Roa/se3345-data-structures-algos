/* Starter code for kar180005.PERT algorithm (Project 4)
 * @author Kevin Roa
 */

package kar180005;

import kar180005.Graph;
import kar180005.Graph.Vertex;
import kar180005.Graph.Edge;
import kar180005.Graph.GraphAlgorithm;
import kar180005.Graph.Factory;

import java.io.File;
import java.util.LinkedList;
import java.util.Scanner;
import java.lang.String;

public class PERT extends GraphAlgorithm<PERT.PERTVertex> {
  LinkedList<Vertex> finishList, revFinishList;

  public static class PERTVertex implements Factory {
    /*
     * ES:    Earliest start time
     * EF:    Earliest finish time
     * LS:    Latest start time
     * LF:    Latest finish time
     * D:     Duration of task
     * Slack: Time difference between ES and LF, 0 = Critical to not delay project
     */
    int es, ef, ls, lf, d, slack;
    boolean seen;   // Used in DFS
    String status;  // Used in DAG test

    public PERTVertex(Vertex u) {
      es = 0;
      ef = 0;
      ls = 0;
      lf = 0;
      d = 0;
      slack = 0;
      seen = false;
      status = "new";
    }

    public PERTVertex make(Vertex u) {
      return new PERTVertex(u);
    }
  }

  // Constructor for kar180005.PERT is private. Create kar180005.PERT instances with static method pert().
  private PERT(Graph g) {
    super(g, new PERTVertex(null));
  }

  // Set the duration of vertex u to d
  public void setDuration(Vertex u, int d) {
    get(u).d = d;
  }

  // Implement the kar180005.PERT algorithm. Returns false if the graph g is not a DAG.
  public boolean pert() {
    // PERT only works when the given graph is a DAG
    if (!isDAGAll()) return false;

    // Fill finishList with vertices in topological order
    topologicalOrder();

    PERTVertex _u, _v;
    int cpl = 0;

    // Loop over every node in topological order
    for (Vertex u : finishList) {
      _u = get(u);

      _u.ef = _u.es + _u.d;

      // Update max ef
      if (_u.ef > cpl) {
        cpl = _u.ef;
      }

      // Loop over every edge (u -> v)
      for (Edge uv : g.outEdges(u)) {
        _v = get(uv.otherEnd(u));

        // Update es of v if ef of u is later
        if (_v.es < _u.ef) {
          _v.es = _u.ef;
        }
      }
    }

    // Set lf in every vertex in the graph to cpl
    for (Vertex u : g) {
      _u = get(u);
      _u.lf = cpl;
    }

    // Loop over the vertices in reverse topological order
    for (Vertex u : revFinishList) {
      _u = get(u);

      _u.ls = _u.lf - _u.d;
      _u.slack = _u.lf - _u.ef;

      // Loop over every edge (v -> u)
      for (Edge vu : g.inEdges(u)) {
        _v = get(vu.otherEnd(u));

        // Update lf of v if ls of u is earlier
        if (_v.lf > _u.ls) {
          _v.lf = _u.ls;
        }
      }
    }

    return true;
  }

  // Test if g is a DAG
  boolean isDAGAll() {
    // Search through every component of g
    for (Vertex u : g) {
      PERTVertex _u = get(u);

      if (_u.status.equals("new")) {
        if (!isDAG(u)) return false;
      }
    }

    return true;
  }

  // Recursively test if a component of g is a DAG
  boolean isDAG(Vertex u) {
    PERTVertex _u = get(u);

    _u.status = "active";

    // Search vertices for cycles
    for (Edge uv : g.outEdges(u)) {
      Vertex v = uv.otherEnd(u);
      PERTVertex _v = get(v);

      // If v is active there is a cycle
      if (_v.status.equals("active")) return false;
      else if (_v.status.equals("new")) {
        if (!isDAG(v)) return false;
      }
    }
    _u.status = "finished";

    return true;
  }

  // Find a topological order of g using DFS
  LinkedList<Vertex> topologicalOrder() {
    finishList = new LinkedList<>();
    revFinishList = new LinkedList<>();

    // Search through every component of g
    for (Vertex u : g) {
      if (!get(u).seen) {
        dfsVisit(u);
      }
    }

    return finishList;
  }

  // Recursively visit each vertex in DFS order
  void dfsVisit(Vertex u) {
    PERTVertex _u = get(u);

    _u.seen = true;

    // For all edges (u -> v)
    for (Edge uv : g.outEdges(u)) {
      Vertex v = uv.otherEnd(u);
      PERTVertex _v = get(v);

      // If v hasn't been visited yet, visit it
      if (!_v.seen) {
        dfsVisit(v);
      }
    }

    // Update topological ordering lists
    finishList.addFirst(u);
    revFinishList.addLast(u);
  }

  // The following methods are called after calling pert().

  // Earliest time at which task u can be completed
  public int ec(Vertex u) {
    return get(u).ef;
  }

  // Latest completion time of u
  public int lc(Vertex u) {
    return get(u).lf;
  }

  // Slack of u
  public int slack(Vertex u) {
    return get(u).slack;
  }

  // Length of a critical path (time taken to complete project)
  public int criticalPath() {
    return get(revFinishList.getFirst()).lf;
  }

  // Is u a critical vertex?
  public boolean critical(Vertex u) {
    return get(u).slack == 0;
  }

  // Number of critical vertices of g
  public int numCritical() {
    int count = 0;

    // Find every critical vertex and update count
    for (Vertex u : g) {
      if (critical(u)) count++;
    }

    return count;
  }

  /* Create a kar180005.PERT instance on g, runs the algorithm.
   * Returns kar180005.PERT instance if successful. Returns null if G is not a DAG.
   */
  public static PERT pert(Graph g, int[] duration) {
    PERT p = new PERT(g);
    for (Vertex u : g) {
      p.setDuration(u, duration[u.getIndex()]);
    }
    // Run kar180005.PERT algorithm.  Returns false if g is not a DAG
    if (p.pert()) {
      return p;
    } else {
      return null;
    }
  }

  public static void main(String[] args) throws Exception {
    String graph = "10 13   1 2 1   2 4 1   2 5 1   3 5 1   3 6 1   4 7 1   5 7 1   5 8 1   6 8 1   6 9 1   7 10 1   8 10 1   9 10 1      0 3 2 3 2 1 3 2 4 1";
    Scanner in;
    // If there is a command line argument, use it as file from which
    // input is read, otherwise use input from string.
    in = args.length > 0 ? new Scanner(new File(args[0])) : new Scanner(graph);
    Graph g = Graph.readDirectedGraph(in);
    g.printGraph(false);

    int[] duration = new int[g.size()];
    for (int i = 0; i < g.size(); i++) {
      duration[i] = in.nextInt();
    }
    PERT p = pert(g, duration);
    if (p == null) {
      System.out.println("Invalid graph: not a DAG");
    } else {
      System.out.println("Number of critical vertices: " + p.numCritical());
      System.out.println("u\tEC\tLC\tSlack\tCritical");
      for (Vertex u : g) {
        System.out.println(u + "\t" + p.ec(u) + "\t" + p.lc(u) + "\t" + p.slack(u) + "\t" + p.critical(u));
      }
    }
  }
}
