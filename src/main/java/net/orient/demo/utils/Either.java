package net.orient.demo.utils;

import java.util.NoSuchElementException;

public class Either<L,R> {
    private R _right;
    private L _left;
    private boolean _isright;
    private Either(L l, R r, boolean right){
        _isright = right;
        if(right){
            _right = r;
        }
        else{
            _left = l;
        }
    }
    public static <L,R> Either<L,R> right(R value){
        return new Either<L, R>(null,value,true);
    }
    public static <L,R> Either<L,R> left(L value){
        return new Either<L, R>(value,null,false);
    }
    public R right(){
        if(!isRight()){
            throw new NoSuchElementException("There is no Right-element: " + this);
        }
        return _right;
    }
    public L left(){
        if(isRight()){
            throw new NoSuchElementException("There is no Left-element: " + this);
        }
        return _left;
    }
    public boolean isRight(){
        return _isright;
    }
    public boolean isLeft(){
        return !isRight();
    }
    @Override
    public String toString(){
        return "(Left:: " + _left + ") (Right:: " + _right + ")";
    }
}
