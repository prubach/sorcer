package sorcer.core.dispatch;

import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.codehaus.plexus.util.dag.DAG;
import org.codehaus.plexus.util.dag.TopologicalSorter;
import sorcer.arithmetic.provider.Adder;
import sorcer.arithmetic.provider.Multiplier;
import sorcer.arithmetic.provider.Subtractor;
import sorcer.service.*;

import java.util.*;

import static sorcer.eo.operator.*;
import static sorcer.eo.operator.in;
import static sorcer.eo.operator.out;

/**
 * SORCER class
 * User: prubach
 * Date: 23.10.13
 */
public class ExertionSorter {


    private final DAG dag;
    private final Map projectMap;
    private final Map<String, String> contextIdsMap;
    private final Map<String, String> revContextIdsMap;
    private List<Exertion> sortedProjects = null;
    private Exertion topLevelProject;


    /**
     * Sort a list of projects.
     * <ul>
     * <li>collect all the vertices for the projects that we want to build.</li>
     * <li>iterate through the deps of each project and if that dep is within
     * the set of projects we want to build then add an edge, otherwise throw
     * the edge away because that dependency is not within the set of projects
     * we are trying to build. we assume a closed set.</li>
     * <li>do a topo sort on the graph that remains.</li>
     * </ul>
     */
    public ExertionSorter(Job topLevelJob)
            throws CycleDetectedException, ContextException {
        dag = new DAG();

        projectMap = new HashMap();

        contextIdsMap = new HashMap<String, String>();

        revContextIdsMap = new HashMap<String, String>();

        addVertex(topLevelJob);

        getMapping(topLevelJob);

        checkParentCycle(topLevelJob);

        List sortedProjects = new ArrayList();
        for (Iterator i = TopologicalSorter.sort(dag).iterator(); i.hasNext(); ) {
            String id = (String) i.next();

            sortedProjects.add(projectMap.get(id));
        }


        this.sortedProjects = Collections.unmodifiableList(sortedProjects);

        int i = 0;
        for (Exertion xrt : this.sortedProjects) {
            System.out.println("Exertion: " + i + " " + xrt.getName());
            i++;
        }

    }

    // two level job composition with PULL and PAR execution
    private static Job createJob(Strategy.Flow flow, Strategy.Access access) throws Exception {
        Task t3 = task(
                "t3",
                sig("subtract", Subtractor.class),
                context("subtract", in("arg/x1", null), in("arg/x2", null),
                        out("result/y", null)));
        Task t4 = task("t4",
                sig("multiply", Multiplier.class),
                context("multiply", in("arg/x1", 10.0), in("arg/x2", 50.0),
                        out("result/y", null)));
        Task t5 = task("t5",
                sig("add", Adder.class),
                context("add", in("arg/x1", 20.0), in("arg/x2", 80.0),
                        out("result/y", null)));

        // Service Composition j1(j2(t4(x1, x2), t5(x1, x2)), t3(x1, x2))
        Job j1 = job("j1", t3, // sig("service", Jobber.class),
                job("j2", t5, t4, strategy(flow, access)),
                pipe(out(t3, "result/y"), in(t4, "arg/x1")),
                pipe(out(t4, "result/y"), in(t3, "arg/x1")),
                pipe(out(t5, "result/y"), in(t3, "arg/x2")));

        return j1;
    }

    private static Job createJob2() throws Exception {

        Task f4 = task("Task_f4", sig("multiply", Multiplier.class),
                context("multiply", input(path("arg/x1"), 2), input(path("arg/x2"), 25 * 2),
                        out(path("result/y1"), null)), strategy(Strategy.Access.PUSH, Strategy.Flow.SEQ, Strategy.Monitor.NOTIFY_ALL, Strategy.Provision.TRUE, Strategy.Wait.TRUE));

        Task f44 = task("Task_f44", sig("multiply", Multiplier.class),
                context("multiply", input(path("arg/x41"), 10.0d), input(path("arg/x42"), 50.0d),
                        out(path("result/y41"), null)));

        Task f5 = task("Task_f5", sig("add", Adder.class),
                context("add", input(path("arg/x3"), 20.0d), input(path("arg/x4"), 80.0d),
                        output(path("result/y2"), null)));

        Task f6 = task("Task_f6", sig("multiply", Multiplier.class),
                context("multiply", input(path("arg/x7"), 11.0d), input(path("arg/x8"), 51.0d),
                        out(path("result/y4"), null)));

        Task f7 = task("Task_f7", sig("multiply", Multiplier.class),
                context("multiply", input(path("arg/x9"), 12.0d), input(path("arg/x10"), 52.0d),
                        out(path("result/y5"), null)));

        Task f9 = task("Task_f9", sig("multiply", Multiplier.class),
                context("multiply", input(path("arg/x11"), 13.0d), input(path("arg/x12"), 53.0d),
                        out(path("result/y6"), null)));

        Task f10 = task("Task_f10", sig("multiply", Multiplier.class),
                context("multiply", input(path("arg/x13"), 14.0d), input(path("arg/x14"), 54.0d),
                        out(path("result/y7"), null)));

        Task f3 = task("Task_f3", sig("subtract", Subtractor.class),
                context("subtract", input(path("arg/x5"), null), input(path("arg/x6"), null),
                        output(path("result/y3"), null)));

        Task f55 = task("Task_f55", sig("add", Adder.class),
                context("add", input(path("arg/x53"), 20.0d), input(path("arg/x54"), 80.0d), output(path("result/y52"), null)));

        Job j8 = job("Job_f8", f9, pipe(out(f10, path("result/y7")), input(f55, path("arg/x54"))), pipe(out(f7, path("result/y5")), input(f55, path("arg/x53"))), f10, f55,
                pipe(out(f9, path("result/y6")), input(f10, path("arg/x13"))));

        Pipe p1 = pipe(out(f4, path("result/y1")), input(f7, path("arg/x9")));

        return job("Job_f1", j8, f3, job("Job_f2", f7, f6, f4, f5),
                pipe(out(f6, path("result/y4")), input(f5, path("arg/x3"))),
                pipe(out(f4, path("result/y1")), input(f3, path("arg/x5"))),
                pipe(out(f5, path("result/y2")), input(f3, path("arg/x6"))), p1);


    }

    public static void main(String[] args) throws Exception {
        //ExertionSorter es = new ExertionSorter(createJob(Strategy.Flow.SEQ, Strategy.Access.PUSH));
        ExertionSorter es = new ExertionSorter(createJob2());
    }

    private void addVertex(Exertion topXrt) throws ContextException {

        String id = topXrt.getId().toString();
        dag.addVertex(id);
        projectMap.put(id, topXrt);
        contextIdsMap.put(id, topXrt.getDataContext().getId().toString());
        revContextIdsMap.put(topXrt.getDataContext().getId().toString(), id);

        for (Iterator i = topXrt.getExertions().iterator(); i.hasNext(); ) {
            Exertion project = (Exertion) i.next();

            id = project.getId().toString();

            if (dag.getVertex(id) != null) {
                throw new ContextException("Project '" + id + "' is duplicated in the reactor");
            }

            dag.addVertex(id);

            projectMap.put(id, project);

            contextIdsMap.put(id, project.getDataContext().getId().toString());
            revContextIdsMap.put(project.getDataContext().getId().toString(), id);

            if (project instanceof Job) {
                addVertex(project);
            }
        }
    }

    private void getMapping(Exertion topXrt) throws CycleDetectedException, ContextException {

        for (Iterator i = topXrt.getExertions().iterator(); i.hasNext(); ) {
            Exertion project = (Exertion) i.next();

            String id = project.getId().toString();

            Map<String, Map<String, String>> metaCtx = project.getDataContext().getMetacontext();
            Map<String, String> ctxMapping = metaCtx.get("cid");
            if (ctxMapping != null) {
                for (Map.Entry<String, String> mapping : ctxMapping.entrySet()) {
                    if (mapping.getValue() != null && mapping.getValue().length() > 0) {
                        String dependencyId = revContextIdsMap.get(mapping.getValue());
                        System.out.println("Map: " + mapping.getKey() + " to " + dependencyId);
                        if (dag.getVertex(dependencyId) != null) {
                            dag.addEdge(id, dependencyId);
                        }
                    }

                }
            }
            if (project instanceof Job) {
                getMapping(project);
            }
        }
    }

    private void checkParentCycle(Exertion topXrt) throws CycleDetectedException, ContextException {
        if (topXrt.getDataContext().getParentID() != null) {
            String parentId = topXrt.getDataContext().getParentID().toString();
            if (dag.getVertex(parentId) != null) {
                // Parent is added as an edge, but must not cause a cycle - so we remove any other edges it has in conflict
                if (dag.hasEdge(parentId, topXrt.getId().toString())) {
                    dag.removeEdge(parentId, topXrt.getId().toString());
                }
                dag.addEdge(topXrt.getId().toString(), parentId);
            }
        }
    }
}
