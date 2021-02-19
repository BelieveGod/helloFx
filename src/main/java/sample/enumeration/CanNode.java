package sample.enumeration;

/**
 * @author LTJ
 * @version 1.0
 * @date 2021/2/19 16:07
 */
public enum CanNode {
    MASTER("主控", 255), MOTOR("驱动器", 100);

    private String nodeName;
    private int nodeId;

    CanNode(String name, int nodeId) {
        this.nodeName = name;
        this.nodeId = nodeId;
    }

    public static CanNode getCanNode(String nodeName){
        CanNode[] values = values();
        for (CanNode value : values) {
            if(value.nodeName.equals(nodeName)){
                return value;
            }
        }
        return null;
    }

    public String getNodeName() {
        return nodeName;
    }

    public int getNodeId() {
        return nodeId;
    }
}
