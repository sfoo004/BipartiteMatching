import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Collections;
import java.util.Hashtable;
import java.util.NoSuchElementException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author stevefoo
 */
public class Bipartite_Matching {

    public static void main(String[] args) {
        for (String str : args) {
            if (args.length == 0) {
                System.out.println("ERROR invalid inputs");
                break;
            }
            try {
                System.out.print(str + "\n");
                double start = System.nanoTime();
                construct(str);
                double stop = System.nanoTime();
                double math = (stop - start) / 1000000000;
                System.out.printf("Elapsed Time: %.4f seconds\n\n", math);
            } catch (IOException e) {
                System.err.println("Error processing" + str);
            }catch (NumberFormatException n) {//checks for wrong input being passed
                System.err.println("Error processing" + str);
            }
        }
    }

    static void construct(String args) throws FileNotFoundException, NoSuchElementException {
        Scanner in = new Scanner(new FileReader(args));
        HashMap<String, ArrayList<String>> matching = new HashMap<>();
        Vertex start = new Vertex("start", 0);
        Vertex sink = new Vertex("sink", 0);
        boolean cut = false;
        while (in.hasNext()) {
            String[] read = in.nextLine().split(":");//tokenize the : then seperate them by the ,
            if (read.length < 2) {
                cut = true;
                continue;
            }
            matching.put(read[0], new ArrayList<String>());
            String[] value = read[1].split(",");//value for the hashmap
            for (String v : value) {
                matching.get(read[0]).add(v);// adds them to the hashmap with the key
            }
            Vertex person = new Vertex(read[0], 1);
            if (!cut) {//source adds person to it's linked list
                start.next.add(person);
                start.matches.add(read[0]);
                person.color="men";//bipartite color
                start.flow_value++;
            } else {//sink adds person to its linked list
                person.next.add(sink);
                sink.prev.add(person);
                sink.matches.add(read[0]);
                person.color="women";//bipartite color
                sink.flow_value++;
            }

            if (sink.flow_value == start.flow_value) {//if source and sink have the same amount of connections
                connection(start, sink, matching);
            }
        }
    }

    static void connection(Vertex start, Vertex sink, HashMap<String, ArrayList<String>> matching) {
        int k = 0;
        boolean maxFlow = false;
        while (!maxFlow) {
            matches(start, sink, matching, k);//function adds hashmap elements to start and sink based on k and does bipartite matching
            bfs(start, sink, matching, k);// does a bfs from the source to the sink
            if (start.flow_value == 0 && sink.flow_value == 0) {// if flow on source and sink is zero then max flow is found
                maxFlow = true;
            }
            k++;

        }
        System.out.println("Everyone matched with top " + k + " preferences:");
        for (Vertex v : start.next) {
            System.out.println(v.name + ": matched to " + v.connection.name + " (rank " + v.level + ")");
        }
        for (Vertex v : sink.prev) {
            System.out.println(v.name + ": matched to " + v.connection.name + " (rank " + v.level + ")");
        }
    }

    static void bfs(Vertex start, Vertex sink, HashMap<String, ArrayList<String>> matching, int k) {
        for (Vertex s : start.next) {
            if (s.taken) {
                continue;
            }
            s.path = start;
            LinkedList<Vertex> q = new LinkedList<>();
            q.add(s);
            while (!q.isEmpty()) {
                Vertex temp = q.poll();
                for (Vertex curr : temp.next) {
                    if (curr == sink && temp.taken) {
                        continue;
                    } else if (curr == sink && !temp.taken) {
                        sink.path = temp;
                        q.clear();
                        break;
                    } else if (curr.path == null) {
                        curr.path = temp;
                        q.add(curr);

                    }
                }
            }
            if (sink.path == null) {
                clear(start, sink);
            } else {
                found(start, sink);//source found a way to sink
                clear(start, sink);
            }
        }
    }

    static void found(Vertex start, Vertex sink) {//backtrack from sink to source
        Vertex backtrack = sink.path;
        while (backtrack.path != start) {
            if (!backtrack.taken) {//
                backtrack.next.remove(sink);
                backtrack.next.add(backtrack.path);//reverses edge to point to previous one
                backtrack.taken(backtrack.path);//declares they are taken for backtrack.path
                backtrack.path.taken(backtrack);//decalres they are taken for backtrack
            } else {
                backtrack.path.next.remove(backtrack);
                backtrack.next.add(backtrack.path);
                backtrack.path.untaken();
            }
            backtrack = backtrack.path;
        }
        sink.flow_value--;
        start.flow_value--;

    }

    static void clear(Vertex start, Vertex sink) {
        start.path = null;
        for (Vertex v : start.next) {
            v.path = null;
        }
        sink.path = null;
        for (Vertex v : sink.prev) {
            v.path = null;
        }
    }

    static void matches(Vertex start, Vertex sink, HashMap<String, ArrayList<String>> matching, int k) {
        for (Vertex v : start.next) {//establish matches
            String match = matching.get(v.name).get(k);
            v.matches.add(match);
        }
        for (Vertex v : sink.prev) {//establish matches
            String match = matching.get(v.name).get(k);
            v.matches.add(match);
        }
        for (Vertex v : start.next) {//establish edges for start
            String new_match = v.matches.get(k);
            for (Vertex sink_v : sink.prev) {
                //makes edge if it matches and next list doesn't contain it already and it's not the same color
                if (sink_v.name.equals(new_match) && sink_v.matches.contains(v.name) && !v.next.contains(sink_v)&& !sink_v.color.equals(v.color)) {
                    v.next.add(sink_v);
                    break;
                }

            }
        }
        for (Vertex v : sink.prev) {//establish edges for sink
            String new_match = v.matches.get(k);
            for (Vertex start_v : start.next) {
                //makes edge if it matches and next list doesn't contain it already and it's not the same color
                if (start_v.name.equals(new_match) && start_v.matches.contains(v.name) && !start_v.next.contains(v)&& !start_v.color.equals(v.color)) {
                    start_v.next.add(v);
                    break;
                }

            }
        }
    }

    static class Vertex {

        ArrayList<String> matches = new ArrayList<>();
        String name = "";
        String color = "";
        LinkedList<Vertex> next = new LinkedList<>();
        LinkedList<Vertex> prev = new LinkedList<>();

        Vertex connection;

        int flow_value = 0;
        boolean taken = false;
        int level = 0;
        Vertex path = null;

        Vertex(String name, int flow_value) {
            this.name = name;
            this.flow_value = flow_value;

        }

        public void taken(Vertex v) {
            this.level = matches.indexOf(v.name) + 1;
            taken = true;
            connection = v;
            flow_value = 0;

        }

        public void untaken() {
            level = 0;
            taken = false;
            connection = null;
            flow_value = 1;
        }

        public void clear_path() {
            path = null;
        }
    }
}
