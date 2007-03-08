/*
 * TypeNode.java
 * 
 * Copyright 2002 Christoph Csallner and Yannis Smaragdakis.
 */
package edu.gatech.cc.jcrasher.planner;

import static edu.gatech.cc.jcrasher.Assertions.check;
import static edu.gatech.cc.jcrasher.Assertions.notNull;

import java.math.BigInteger;

import edu.gatech.cc.jcrasher.plans.expr.Expression;
import edu.gatech.cc.jcrasher.types.TypeGraph;
import edu.gatech.cc.jcrasher.types.TypeGraphImpl;

import static java.math.BigInteger.ZERO;

/**
 * Node to access the plans of a type (sub-) plan space up to a given maximal
 * chaining depth.
 * <ul>
 * <li>Child plan spaces are all FunctionNode or ValueNode.
 * <li>Knows how to map an index to an index of one of its child-plans.
 * 
 * Expression space depth recursion can only stop at a type, as a type decides whether
 * to recurse to another function or stick with (the predifened) values.
 * 
 * @author csallner@gatech.edu (Christoph Csallner)
 */
public abstract class TypeNode<T> implements PlanSpaceNode<T> {

	protected final TypeGraph typeGraph = TypeGraphImpl.instance();
	
  /**
   * Child types, i.e. victim and param types up to our max depth - 1
   */
  private PlanSpaceNode<T>[] children;

  /**
   * Examples:
   * <ul>
   * <li>(2, 7, 9) implies [0..2], [3..7], [8..9]
   * <li>(-1, 9, 19, 19, 24) implies [0..-1], [0..9], [10..19], [20..19], [20..24]
   * </ul>
   */
  protected int[] childRanges;
  
  /**
   * Size of each child's plan space (given their max depth), e.g.:
   * <ul>
   * <li>(3, 5, 2) implies own size of 10
   * <li>(0, 10, 10, 0, 5) implies own size of 25
   * </ul>
   */
  protected int[] childSizes;
    
  protected int planSpaceSize = 0; // own plan space size = sum of childrens'

  /**
   * Map childIndex to the lowest index that belongs to this child, e.g. for
   * childRanges = (-1, 9, 19, 19, 24)
   * <ul>
   * <li>lowestRangeHit(0) = 0
   * <li>lowestRangeHit(1) = 0
   * <li>lowestRangeHit(4) = 20
   * </ul>
   * 
   * @param childIndex 0, .., (children.length - 1)
   */
  private int lowestRangeHit(int childIndex) {
    check(childIndex >= 0);
    check(childIndex < children.length);

    if (childRanges == null) {
      getPlanSpaceSize();
    }

    int res = 0; // first index starts at zero

    if (childIndex > 0) {
      res = childRanges[childIndex - 1] + 1;
    }

    return res;
  }


  /**
   * Precond: 0 <= childIndex < children.length Postcond: no side effect
   * 
   * Map childIndex to the highest index that belongs to this child, e.g. for
   * childRanges = (-1, 9, 19, 19, 24) highestRangeHit(0) = -1
   * highestRangeHit(1) = 9 highestRangeHit(4) = 24
   */
  private int highestRangeHit(int childIndex) {
    check(childIndex >= 0);
    check(childIndex < children.length);

    if (childRanges == null) {
      getPlanSpaceSize();
    }

    return childRanges[childIndex];
  }


  /**
   * Sets the children. To be called by extending classes only.
   * 
   * @param parameters The children to set
   */
  protected void setChildren(final PlanSpaceNode<T>[] pChildren) {
    this.children = pChildren;
  }

  protected PlanSpaceNode<T>[] getChildren() {
    return children;
  }

  /**
   * Precond: true Postcond: cached sizes of all sub plan spaces to speed up
   * getPlan(int)
   * 
   * A type's plan space size is the sum of its child plan spaces.
   * 
   * @return size of this sub plan space = nr different plans this plan space
   *         can return via getPlan(int)
   */
  public int getPlanSpaceSize() {
    notNull(children);
    
    /* Compute childrens' and own plan space sizes */
    if (childSizes == null) { // first call
      childSizes = new int[children.length];

      /* Compute child sizes recursively */
      for (int i = 0; i < children.length; i++) {
        childSizes[i] = children[i].getPlanSpaceSize();
      }

      /* Add up childrens' plan space sizes */
      childRanges = new int[children.length];
      int res = 0; // no children: one plan for static non-arg meth
      for (int i = 0; i < children.length; i++) {
        res += childSizes[i]; // partial sum so far
        childRanges[i] = res - 1; // compute index-range for each sub space
      }
      planSpaceSize = res;
    }
    
    if (planSpaceSize<0) {
      System.out.println("TODO: int overflow.");
      planSpaceSize = Integer.MAX_VALUE;
    }

    return planSpaceSize;
  }


  /**
   * Precond: 0 <= planIndex < getPlanSpaceSize() Postcond: no side-effects
   * 
   * Retrieve childrens' plan according to canonical ordering. For e.g. sub
   * spaces of sizes (3, 5, 2) --> childRanges = [0..2], [3..7], [8..9] [0..2] -
   * 0 = [0..2] [3..7] - 3 = [0..4] [8..9] - 8 = [0..1]
   * 
   * @param planIndex the index of the plan according to the node's canonical
   *          order, taken from [0..getPlanSpaceSize()-1]
   * @return childrens' plans according to the ordering semantics, never null
   */
  public Expression<? extends T> getPlan(int planIndex, Class<?> testeeType) {
    check(planIndex >= 0);
    check(planIndex < getPlanSpaceSize());

    /* Make sure children and own sizes are cached */
    if (childSizes == null) {
      getPlanSpaceSize();
    }

    int childIndex = getChildIndex(planIndex);
    int childPlanIndex = getChildPlanIndex(childIndex, planIndex);

    return children[childIndex].getPlan(childPlanIndex, testeeType);
  }


  /**
   * @return index into child's childIndex plan space at index planindex
   */
  protected int getChildPlanIndex(int childIndex, int planIndex) {

    return planIndex - lowestRangeHit(childIndex);
  }


  /**
   * Get index into children array, that is hit by this planIndex
   */
  protected int getChildIndex(int planIndex) {

    int i = 0; // find child, whose sub range is hit
    /*
     * [7..7] [8..7] [8..8] 7 --> first range 8 --> third range
     */
    while ((planIndex < lowestRangeHit(i)) || (planIndex > highestRangeHit(i))) {
      i++;
    }
    return i;
  }


  /**
   * Retrieve child plan space node
   */
  protected PlanSpaceNode<?> getChild(int childIndex) {
    check(childIndex >= 0);
    check(childIndex < children.length);

    return children[childIndex];
  }
}
