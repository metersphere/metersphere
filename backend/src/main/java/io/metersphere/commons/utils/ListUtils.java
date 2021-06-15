package io.metersphere.commons.utils;


import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/6/15 10:48 上午
 */
public class ListUtils<T> {
    public void set(List<T> list, int index, T t, T emptyObj){
        if(list.size()<=index){
            for(int i = 0;i <= index; i++){
                if(i >= list.size()){
                    list.add(emptyObj);
                }
            }
        }
        list.set(index,t);
    }
}
