package com.automapart.autobuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.automapart.AutoMapArt;
import com.automapart.autobuilder.utils.Utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class Astar {
    private static final int MAX_LENGTH = 200;
    private static final int TIMEOUT = 5000;
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
                if (System.currentTimeMillis() - startTime >= TIMEOUT) {
                    return new BlockPos[0];
                }
                checkTime = 1000;
            } else {
                checkTime--;
            }

            Node current = getLowestCostNode(openSet);

            openSet.remove(current);
            closedSet.add(current);

            List<Node> neighbors = getNeighbors(mc, current, goal);
            if (current.gScore >= MAX_LENGTH || neighbors.isEmpty()) {
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

        return new BlockPos[0];
    }

    private BlockPos[] createPath(Node node) {
        ArrayList<BlockPos> blockPos = new ArrayList<>();
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
        return Math.sqrt(Math.pow((double) one.x - two.x, 2) + Math.pow((double) one.y - two.y, 2)
                + Math.pow((double) one.z - two.z, 2));
    }

    private Node getLowestCostNode(Set<Node> openSet) {
        return openSet.stream().min(Comparator.comparingDouble(node -> node.fScore)).orElse(new Node(0, 0, 0));
    }

    private List<Node> getNeighbors(MinecraftClient mc, Node node, BlockPos goal) {
        List<Node> neighbors = new ArrayList<>();

        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dz = -1; dz <= 1; dz += 2) {
                if (!mc.world.isChunkLoaded(node.x + dx, node.z + dz)) {
                    return new ArrayList<>(0);
                }
                for (int dy = -1; dy <= 1; dy += 2) {
                    BlockPos pos = new BlockPos(node.x + dx, node.y + dy, node.z + dz);
                    if (goal.equals(pos)) {
                        return new ArrayList<>(0);
                    }

                    if (checkIfValidNeighbor(node, pos, dx != 0 && dz != 0))
                        neighbors.add(new Node(pos));
                }
            }
        }

        return neighbors;
    }

    private boolean checkIfValidNeighbor(Node node, BlockPos pos, boolean isCorner) {
        return (!Utils.canWalk(pos) || (isCorner && (!Utils.canWalk(node.x, pos.getY(), pos.getZ())
                || !Utils.canWalk(pos.getX(), pos.getY(), node.z))));

    }

}

class Node implements Comparable<Node> {
    int x;
    int y;
    int z;
    int gScore;
    double fScore;
    Node previous;

    public Node(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Node(Vec3i pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
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
