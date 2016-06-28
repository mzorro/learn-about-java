/**
 * Created by Zorro on 6/19 019.
 */
package me.mzorro.ds.tree;


public class RBTree extends BSearchTree {

    static class RBNode extends TNode {
        private boolean isRed;

        public RBNode(int val, boolean isRed) {
            super(val);
            this.isRed = isRed;
        }

        @Override
        public RBNode getLeft() {
            return (RBNode) super.getLeft();
        }

        @Override
        public RBNode getRight() {
            return (RBNode) super.getRight();
        }

        @Override
        public RBNode getParent() {
            return (RBNode) super.getParent();
        }

        @Override
        public RBNode getGrantParent() {
            return (RBNode) super.getGrantParent();
        }

        @Override
        public String toString() {
            return super.toString() + (isRed ? "R" : "B");
        }
    }

    @Override
    public RBNode getRoot() {
        return (RBNode) super.getRoot();
    }

    /**
     * 左旋操作
     * @param p 左旋轴点
     */
    protected void leftRotate(TNode p) {
        if (p == null) {
            throw new NullPointerException();
        }
        if (p.getRight() == null) {
            throw new NullPointerException("左旋轴点右子不能为空！");
        }
        TNode child = p.getRight();
        replaceNode(p, child);

        p.setRight(child.getLeft());
        if (p.getRight() != null) p.getRight().setParent(p);

        child.setLeft(p);
        p.setParent(child);
    }

    /**
     * 右旋操作
     * @param p 右旋轴点
     */
    protected void rightRotate(TNode p) {
        if (p == null) {
            throw new NullPointerException();
        }
        if (p.getLeft() == null) {
            throw new NullPointerException("右旋轴点左子不能为空！");
        }
        TNode child = p.getLeft();
        replaceNode(p, child);

        p.setLeft(child.getRight());
        if (p.getLeft() != null) p.getLeft().setParent(p);

        child.setRight(p);
        p.setParent(child);
    }

    /**
     * 插入后的修复操作
     * @param p 修复的起点
     */
    private void insertFixUp(RBNode p) {
        // 1.根节点不需要修复，涂黑即可
        if (p.getParent() == null) {
            p.isRed = false;
            return;
        }

        // 2.父节点是黑色，不需要修复
        if (!p.getParent().isRed) return;

        // 3.父节点是红色(由于根节点是黑色，p.parent不可能是根节点)
        if (p.getParent().getParent() == null) {
            throw new IllegalStateException("出现错误，根节点为红色？!");
        }
        // 求出叔父节点
        RBNode uncle = p.getParent() == p.getGrantParent().getLeft() ?
                p.getGrantParent().getRight() : p.getGrantParent().getLeft();

        // 3.1.叔父节点是红色
        if (uncle != null && uncle.isRed) {

            // 将父节点和叔父节点涂黑
            p.getParent().isRed = false;
            uncle.isRed = false;

            // 将祖父节点涂红
            p.getParent().getParent().isRed = true;

            // 从祖父节点重新开始修复
            insertFixUp(p.getGrantParent());
        } else {// 3.2.叔父节点是黑色(null也是黑色)

            // 3.2.1.当前节点是其父节点的左子，父节点是祖父节点的右子
            if (p.isLeftChild() && p.getParent().isRightChild()) {

                RBNode parent = p.getParent();

                // 以父节点为轴进行右旋
                rightRotate(parent);

                // 从父节点重新开始修复
                insertFixUp(parent);

                // 3.2.2.当前节点是其父节点的右子，父节点是祖父节点的左子
            } else if (p.isRightChild() && p.getParent().isLeftChild()) {

                RBNode parent = p.getParent();

                // 以父节点为轴进行左旋
                leftRotate(parent);

                // 从父节点重新开始修复
                insertFixUp(parent);
            } else {
                // 现在可以确认当前节点和其父节点在同一方向（都是左子或右子）
                // 3.2.1和3.2.2中旋转后就是这样的情况

                // 将父节点染黑，祖父节点染红
                p.getParent().isRed = false;
                p.getGrantParent().isRed = true;

                // 3.2.3.当前节点是其父节点的左子，父节点是祖父节点的左子
                if (p.isLeftChild() && p.getParent().isLeftChild()) {

                    // 以祖父节点为轴进行右旋
                    rightRotate(p.getGrantParent());

                    // 3.2.4.当前节点是其父节点的右子，父节点是祖父节点的右子
                } else {//p.isRightChild() && p.getParent().isRightChild()

                    // 以祖父节点为轴进行左旋
                    leftRotate(p.getGrantParent());
                }

                // 修复结束
            }
        }
    }

    @Override
    public void doInsert(int value) {
        if (getRoot() == null) {
            // 根节点为黑色
            setRoot(new RBNode(value, false));
        } else {
            // 新插入的节点为红色
            RBNode newNode = new RBNode(value, true);
            insertNode(newNode);
            // 进行插入修复
            insertFixUp(newNode);
        }
    }

    @Override
    protected boolean doDelete(int value) {
        // 红黑树的删除操作暂时没有实现
        return false;
    }

    public static void main(String[] args) {
        RBTree rbTree = new RBTree();
        int[] input = new int[] { 12,1,9,2,0,11,7,19,4,15,18,5,14,13,10,16,6,3,8,17 };
        for (int i : input) {
            rbTree.insert(i);
        }
        for (int i : input) {
            rbTree.delete(i);
        }
    }
}
