package net.orient.demo.web;

public class Identity {
    private static final String UNKNOWN = "UNKNOWN";
    private String localId;

    public Identity(){
        this(UNKNOWN,UNKNOWN);
    }

    public Identity(String _localId, String _universalId){
        personId = _universalId;
        localId = _localId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    private String personId;
}
