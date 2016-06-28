/**
 * Created by Zorro on 6/20 020.
 */
package me.mzorro.ds.tree;

/**
 * 非平衡二叉查找树
 */
public class BSearchTree extends BinaryTree {
    /**
     * 以newNode替换oldNode
     * 这里只改变与父节点的关系，不改变子节点
     * @param oldNode
     * @param newNode
     */
    protected void replaceNode(TNode oldNode, TNode newNode) {
        if (oldNode == getRoot()) {
            setRoot(newNode);
        } else if (oldNode.isLeftChild()) {
            oldNode.getParent().setLeft(newNode);
        } else {
            oldNode.getParent().setRight(newNode);
        }
        if (newNode != null) {
            newNode.setParent(oldNode.getParent());
        }
    }

    /**
     * 插入节点
     * @param newNode 新的节点
     */
    protected void insertNode(TNode newNode) {
        TNode p = getRoot(), q;
        if (p == null) throw new NullPointerException("root is null");
        if (newNode == null) throw new NullPointerException("insert null");
        do {
            q = p;
            if (newNode.getValue() > p.getValue()) {
                p = p.getRight();
            } else {
                p = p.getLeft();
            }
        } while (p != null);
        if (newNode.getValue() > q.getValue()) {
            q.setRight(newNode);
        } else {
            q.setLeft(newNode);
        }
        newNode.setParent(q);
    }

    /**
     * 删除节点
     * @param node 待删除的节点
     */
    protected void deleteNode(TNode node) {
        if (node == null) {
            throw new NullPointerException("delete null");
        }
        // 1.待删除节点为叶子节点，直接删除
        if (node.getLeft() == null && node.getRight() == null) {
            replaceNode(node, null);

            // 2.待删除节点只有一个子节点，以子节点替换
        } else if (node.getLeft() == null || node.getRight() == null) {
            TNode child = node.getLeft() == null ? node.getRight() : node.getLeft();
            replaceNode(node, child);

            // 3.待删除节点有两个子节点，找到左子中最大节点替换
        } else {
            TNode p = node.getLeft();
            while (p.getRight() != null) p = p.getRight();
            replaceNode(p, p.getLeft());
            p.setLeft(node.getLeft());
            p.setRight(node.getRight());
            if (node.getLeft() != null) node.getLeft().setParent(p);
            if (node.getRight() != null) node.getRight().setParent(p);
            replaceNode(node, p);
        }
    }

    protected TNode findNode(int value) {
        TNode p = getRoot();
        while (p != null) {
            if (p.getValue() > value) {
                p = p.getLeft();
            } else if (p.getValue() < value) {
                p = p.getRight();
            } else {
                return p;
            }
        }
        return null;
    }

    @Override
    protected void doInsert(int value) {
        TNode newNode = new TNode(value);
        if (getRoot() == null) {
            setRoot(newNode);
        } else {
            insertNode(newNode);
        }
    }

    @Override
    protected boolean doDelete(int value) {
        TNode node = findNode(value);
        if (node != null) deleteNode(node);
        return node != null;
    }

    public static void main(String[] args) {
        BSearchTree searchTree = new BSearchTree();
        int[] input = new int[] { 4,3,7,5,2,8,1,9,0,6 };
        for (int i : input) {
            searchTree.insert(i);
        }

        input = new int[] { 4,3,7,0,6,5,2,8,1,9 };
        for (int i : input) {
            searchTree.delete(i);
        }
    }
}
