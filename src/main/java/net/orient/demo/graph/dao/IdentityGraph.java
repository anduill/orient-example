package net.orient.demo.graph.dao;


import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;
import net.orient.demo.web.Identity;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

public class IdentityGraph {
    private static final String PERSON_PREFIX = "PERSON_UUID";
    private static final String TIMESTAMP = "TIMESTAMP";
    private static final String ID = "ID";
    private static final String TYPE = "TYPE";
    private static final String LINK = "LINK";
    private static final String UNIVERSAL = "UNIVERSAL";

    private OrientGraph identityGraph;

    public IdentityGraph(String orientURL, String orientUser, String orientPW){
        identityGraph = new OrientGraph(orientURL, orientUser, orientPW);
    }
    public Identity getUUID(String localId){
        List<Vertex> vertices = Lists.newArrayList(identityGraph.getVertices(ID, localId));
        for(Vertex v : vertices){
            for(Vertex u : v.getVertices(Direction.OUT)){
                return new Identity(localId, u.getProperty(PERSON_PREFIX) != null ?
                    u.getProperty(PERSON_PREFIX).toString()
                : "UNKNOWN");
            }
        }
        return new Identity();
    }
    public void addSingleLocal(String locId){
        Optional<Vertex> localVertex = getLocalIDVertex(locId);
        if(!localVertex.isPresent()){
            Vertex l = makeLocalVertex(locId);
            Vertex p = makePersonVertex(locId);
            linkToPersonVertex(p,l);
            identityGraph.commit();
        }
    }
    public void addLink(String localId1, String localId2){
        Optional<Vertex> localVertex1 = getLocalIDVertex(localId1);
        Optional<Vertex> localVertex2 = getLocalIDVertex(localId2);

        if(!localVertex1.isPresent() && !localVertex2.isPresent()){
            //Neither id is present in the system...we have to create the two vertices and link them both to a single Universal ID.
            OrientVertex loc1 = makeLocalVertex(localId1);
            OrientVertex loc2 = makeLocalVertex(localId2);
            OrientVertex personVertex = makePersonVertex(localId1);
            linkToPersonVertex(personVertex, loc1, loc2);
        } else if(!localVertex1.isPresent() && localVertex2.isPresent()){
            //One vertex is present, but the other is not
            OrientVertex loc1 = makeLocalVertex(localId1);
            Vertex personVertex = getPersonVertex(localVertex2.get());
            linkToPersonVertex(personVertex, loc1);
        } else if(localVertex1.isPresent() && !localVertex2.isPresent()){
            //One vertex is present, but the other is not
            OrientVertex loc2 = makeLocalVertex(localId2);
            Vertex personVertex = getPersonVertex(localVertex1.get());
            linkToPersonVertex(personVertex, loc2);
        } else{
            //Both vertices are present in the system...so we have to resolve them to a single person vertex
            Vertex person1 = getPersonVertex(localVertex1.get());
            Vertex person2 = getPersonVertex(localVertex2.get());
            if(!person1.getProperty(PERSON_PREFIX).equals(person2.getProperty(PERSON_PREFIX))){
                Long p1TS = person1.getProperty(TIMESTAMP);
                Long p2TS = person2.getProperty(TIMESTAMP);
                if(p1TS <= p2TS){
                    cleanPersonLinks(person1, person2);
                } else {
                    cleanPersonLinks(person2, person1);
                }
            }
        }
        identityGraph.commit();
    }
    /**VERY DANGEROUS OPERATION...used here as convenience for testing**/
    public void clearGraph(){
        for(Edge edge : identityGraph.getEdges()){
            identityGraph.removeEdge(edge);
        }
        for(Vertex v : identityGraph.getVertices()){
            identityGraph.removeVertex(v);
        }
        identityGraph.commit();
    }
    private void cleanPersonLinks(Vertex refPerson, Vertex oldPerson) {
        for(Vertex v : oldPerson.getVertices(Direction.IN)){
            for(Edge e : v.getEdges(Direction.OUT)){
                identityGraph.removeEdge(e);
            }
            linkToPersonVertex(refPerson, v);
        }
        identityGraph.removeVertex(oldPerson);
    }

    private Vertex getPersonVertex(Vertex localVertex){
        for(Vertex v : localVertex.getVertices(Direction.OUT)){
            return v;
        }
        return null;//TODO: do something smarter for cases where it might be possible to not have a person vertex
    }
    private void linkToPersonVertex(Vertex personVertex, Vertex...locs) {
        for(Vertex loc : locs){
            identityGraph.addEdge(PERSON_PREFIX + "::" + loc.toString(), loc, personVertex, UNIVERSAL);
        }
    }

    private OrientVertex makePersonVertex(String id) {
        Map<String, Object> properties = Maps.newHashMap();
        properties.put(PERSON_PREFIX, PERSON_PREFIX + "::" +id);
        Long ts = DateTime.now().getMillis();
        properties.put(TIMESTAMP, ts);
        return identityGraph.addVertex(id, properties);
    }

    private OrientVertex makeLocalVertex(String localId) {
        Map<String,String> properties = Maps.newHashMap();
        properties.put(ID, localId);
        return identityGraph.addVertex(localId, properties);
    }

    private Optional<Vertex> getLocalIDVertex(String localID){
        List<Vertex> vertices = Lists.newArrayList(identityGraph.getVertices(ID, localID));
        if(!vertices.isEmpty()){
            return Optional.fromNullable(vertices.get(0));
        }
        return Optional.absent();
    }
    public List<Vertex> getAllVertices(){
        return Lists.newArrayList(identityGraph.getVertices());
    }
}
