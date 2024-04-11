package com.automapart.autobuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.automapart.AutoMapArt;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class Astar {
    private final int maxLength = 200;
    private final int timeout = 5000;
    private MinecraftClient mc;

    public Astar(MinecraftClient mc) {
        this.mc = mc;
    }

    public BlockPos[] findPath(BlockPos start, BlockPos goal) {

        Set<Node> openSet = new HashSet<>();
        Set<Node> closedSet = new HashSet<>();
        Node startNode = new Node(start.getX(), start.getY(), start.getZ());
        Node endNode = new Node(goal.getX(), goal.getY(), goal.getZ());
        if (start.equals(goal)) {
            return createPath(startNode);
        }

        openSet.add(startNode);

        long startTime = System.currentTimeMillis();
        int checkTime = 1000;

        int movementsConsidered = 0;

        while (!openSet.isEmpty()) {
            movementsConsidered++;
            if (checkTime == 0) {
                if (System.currentTimeMillis() - startTime >= timeout) {
                    return null;
                }
                checkTime = 1000;
            } else {
                checkTime--;
            }

            Node current = getLowestCostNode(openSet);

            openSet.remove(current);
            closedSet.add(current);

            List<Node> neighbors = getNeighbors(mc, current, goal);
            if (current.gScore >= maxLength || neighbors == null) {
                AutoMapArt.LOGGER.debug("Movements Considered " + movementsConsidered + " Time taken "
                        + (System.currentTimeMillis() - startTime));
                return createPath(current);
            }

            for (Node neighbor : neighbors) {
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                int tentativeG = current.gScore + 1;

                if (!openSet.contains(neighbor) || tentativeG < neighbor.gScore) {
                    neighbor.gScore = tentativeG;
                    neighbor.fScore = neighbor.gScore + heuristic(neighbor, endNode);
                    neighbor.previous = current;

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return null;
    }

    private BlockPos[] createPath(Node node) {
        ArrayList<BlockPos> blockPos = new ArrayList<BlockPos>();
        Node currentPlace = node;
        while (currentPlace != null) {
            blockPos.add(new BlockPos(currentPlace.x, currentPlace.y, currentPlace.z));
            currentPlace = currentPlace.previous;
        }
        blockPos.trimToSize();
        BlockPos[] outputPath = new BlockPos[blockPos.size()];
        for (int i = 0; i < blockPos.size(); i++) {
            outputPath[outputPath.length - i - 1] = blockPos.get(i);
        }
        return outputPath;
    }

    private double heuristic(Node one, Node two) {
        return Math.sqrt(Math.pow(one.x - two.x, 2) + Math.pow(one.y - two.y, 2) + Math.pow(one.z - two.z, 2));
    }

    private Node getLowestCostNode(Set<Node> openSet) {
        return openSet.stream().min(Comparator.comparingDouble(node -> node.fScore)).orElse(null);
    }

    private List<Node> getNeighbors(MinecraftClient mc, Node node, BlockPos goal) {
        List<Node> neighbors = new ArrayList<>();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (!mc.world.isChunkLoaded(node.x + dx, node.z + dz)) {
                    return null;
                }
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue; // Skip the current node
                    }
                    int newX = node.x + dx;
                    int newY = node.y + dy;
                    int newZ = node.z + dz;
                    if (goal.getX() == newX && goal.getY() == newY && goal.getZ() == newZ) {
                        return null;
                    }
                    if (!Utils.canWalk(newX, newY, newZ)) {
                        continue;
                    }

                    if (dx != 0 && dz != 0) {
                        if (!Utils.canWalk(node.x, newY, newZ)
                                || !Utils.canWalk(newX, newY, node.z))
                            continue;
                    }

                    Node newNode = new Node(newX, newY, newZ);

                    neighbors.add(newNode);
                }
            }
        }

        return neighbors;
    }

}

class Node implements Comparable<Node> {
    int x, y, z, gScore;
    double fScore;
    Node previous;

    public Node(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int compareTo(Node node) {
        return (int) (this.fScore - node.fScore);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Node node = (Node) obj;
        return x == node.x && y == node.y && z == node.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
