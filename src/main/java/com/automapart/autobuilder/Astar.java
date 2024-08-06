package com.automapart.autobuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;

import com.automapart.AutoMapArt;
import com.automapart.autobuilder.utils.Utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class Astar {
    private Astar() {
    }

    private static final int MAX_LENGTH = 200;
    private static final int TIMEOUT = 5000;

    /***
     * 
     * @param start starting location for the path
     * @param goal  end location of the path
     * @param mc    the minecraft client
     * @param path  the list that will be used for the path
     * @return
     */
    public static BlockPos[] findPath(BlockPos start, BlockPos goal, MinecraftClient mc) {
        Set<Node> openSet = new HashSet<>();
        Set<Node> closedSet = new HashSet<>();
        Node startNode = new Node(start);
        Node endNode = new Node(goal);
        if (start.equals(goal)) {
            return reconstructPath(startNode);
        }
        openSet.add(startNode);

        // metrics
        long startTime = System.currentTimeMillis();
        int checkTime = 1000;
        int movementsConsidered = 0;

        while (!openSet.isEmpty()) {
            movementsConsidered++;

            if (checkTime == 0) {
                if (System.currentTimeMillis() - startTime >= TIMEOUT) {
                    return null;
                }
                checkTime = 1000;
            } else {
                checkTime--;
            }

            Node current = getLowestCostNode(openSet);
            openSet.remove(current);
            closedSet.add(current);

            List<Node> neighbors = getValidNeighbors(mc, current);

            if (current.gScore >= MAX_LENGTH || neighbors.isEmpty() || current.equals(endNode)) {
                long totalTime = System.currentTimeMillis() - startTime;
                AutoMapArt.LOGGER
                        .debug("Movements considered {} Time Taken {}", movementsConsidered, totalTime);
                return reconstructPath(current);
            }
            addNeighbors(current, endNode, neighbors, openSet, closedSet);

        }

        return new BlockPos[0];
    }

    private static void addNeighbors(Node current, Node endNode, List<Node> neighbors, Set<Node> openSet,
            Set<Node> closedSet) {
        for (Node neighbor : neighbors) {
            if (closedSet.contains(neighbor)) {
                continue;
            }

            int tentativeG = current.gScore + 1;
            boolean openSetDoesntContain = openSet.contains(neighbor);
            if (openSetDoesntContain || tentativeG < neighbor.gScore) {
                neighbor.gScore = tentativeG;
                neighbor.fScore = neighbor.gScore + heuristic(neighbor, endNode);
                neighbor.previous = current;

                if (openSetDoesntContain) {
                    openSet.add(neighbor);
                }
            }
        }
    }

    private static BlockPos[] reconstructPath(Node node) {
        ArrayList<BlockPos> blockPositions = new ArrayList<>();
        Node currentNode = node;
        while (currentNode != null) {
            blockPositions.add(new BlockPos(currentNode.pos));
            currentNode = currentNode.previous;
        }
        Collections.reverse(blockPositions);
        BlockPos[] outputPath = new BlockPos[blockPositions.size()];
        return blockPositions.toArray(outputPath);
    }

    private static double heuristic(Node one, Node two) {
        return Math.sqrt(one.pos.getSquaredDistance(two.pos));
    }

    private static Node getLowestCostNode(Set<Node> openSet) {
        return openSet.stream().min(Comparator.comparingDouble(node -> node.fScore)).orElse(null);
    }

    private static List<Node> getValidNeighbors(MinecraftClient mc, Node node) {
        List<Node> neighbors = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (!mc.world.isChunkLoaded(node.pos.getX() + dx, node.pos.getZ() + dz)) {
                    return new ArrayList<>();
                }
                for (int dy = -1; dy <= 1; dy++) {
                    Vec3i newPosition = node.pos.add(dx, dy, dz);
                    if (isValidNeighbor(new Vec3i(dx, dy, dz), newPosition)) {
                        neighbors.add(new Node(newPosition));
                    }
                }
            }
        }
        return neighbors;
    }

    private static boolean isValidNeighbor(Vec3i delta, Vec3i newPosition) {
        if (delta.equals(Vec3i.ZERO) || !Utils.canWalk(newPosition)) {
            return false;
        }
        boolean diagonal = delta.getX() != 0 && delta.getZ() != 0;
        boolean xSideWalkable = Utils.canWalk(newPosition.add(-delta.getX(), 0, 0));
        boolean zSideWalkable = Utils.canWalk(newPosition.add(0, 0, -delta.getZ()));
        return !diagonal || (xSideWalkable && zSideWalkable);
    }
}

class Node implements Comparable<Node> {
    Vec3i pos;
    int gScore;
    double fScore;
    Node previous;

    public Node(Vec3i pos) {
        this.pos = pos;
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
        return node.pos.equals(this.pos);
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }
}

class OutOfTimeException extends Exception {
    public OutOfTimeException(String errorMessage) {
        super(errorMessage);
    }
}
