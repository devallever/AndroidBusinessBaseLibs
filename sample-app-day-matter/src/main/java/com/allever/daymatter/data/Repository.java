package com.allever.daymatter.data;

import android.content.Context;
import android.util.Log;

import com.allever.daymatter.R;
import com.allever.daymatter.bean.ItemSlidMenuSort;
import org.litepal.LitePal;
import java.util.ArrayList;
import java.util.List;
import app.allever.android.lib.core.app.App;
import app.allever.android.lib.core.helper.ExecutorHelper;

/**
 * Created by Allever on 18/5/26.
 */

public class Repository implements DataSource {

    private static final String TAG = "Repository";

    private Repository(){}

    public static DataSource getIns() {
        return Holder.INSTANCE;
    }

    private static class Holder{
        private static Repository INSTANCE = new Repository();
    }

    @Override
    public String getEventTitle(int id) {
        return null;
    }

    @Override
    public void addDefaultSortData(Context context) {
        Log.d(TAG, "addDefaultSortData: ");
        Event.Sort sortLife = new Event.Sort();
        sortLife.setId(1);
        sortLife.setName(context.getResources().getString(R.string.dm_sort_life));
        sortLife.setDefaultSort(true);
        sortLife.save();

        Event.Sort sortWork = new Event.Sort();
        sortWork.setId(2);
        sortWork.setName(context.getResources().getString(R.string.dm_sort_work));
        sortWork.setDefaultSort(true);
        sortWork.save();

        Event.Sort sortMemoryDay = new Event.Sort();
        sortMemoryDay.setId(3);
        sortMemoryDay.setName(context.getResources().getString(R.string.dm_sort_memory_day));
        sortMemoryDay.setDefaultSort(true);
        sortMemoryDay.save();

    }

    @Override
    public List<ItemSlidMenuSort> getSlidMenuSortData(Context context) {
        List<ItemSlidMenuSort> list = new ArrayList<>();

        //第一项为全部
        ItemSlidMenuSort firstItemSlidMenuSort = new ItemSlidMenuSort();
        firstItemSlidMenuSort.setCount(LitePal.findAll(Event.class).size());
        firstItemSlidMenuSort.setName(context.getString(R.string.dm_all));
        firstItemSlidMenuSort.setId(0);
        list.add(firstItemSlidMenuSort);

        //查询分类数
        List<Event.Sort> sortList = LitePal.findAll(Event.Sort.class);
        Log.d(TAG, "getSlidMenuSortData: sort size = " + sortList.size());

        for (Event.Sort sort: sortList){
            ItemSlidMenuSort itemSlidMenuSort = new ItemSlidMenuSort();
            itemSlidMenuSort.setId(sort.getId());
            itemSlidMenuSort.setName(sort.getName());
            Log.d(TAG, "getSlidMenuSortData: id = " + sort.getId());

            //查询该id的事件数
            List<Event> eventList = LitePal.where("sortId = " + sort.getId()).find(Event.class);
            if (eventList != null){
                itemSlidMenuSort.setCount(eventList.size());
            }else {
                itemSlidMenuSort.setCount(0);
            }
            list.add(itemSlidMenuSort);
        }

        return list;
    }

    @Override
    public boolean saveEvent(String eventTitle, int year, int month, int day, int weekday, int sortId, boolean isTop, int repeatType, boolean isEnd, int endYear, int endMonth, int endDay, int endWeekday) {
        Event event = new Event();
        event.setTitle(eventTitle);
        event.setYear(year);
        event.setMonth(month);
        event.setDay(day);
        event.setWeekDay(weekday);
        event.setSortId(sortId);
        event.setTop(isTop);
        event.setRepeatType(repeatType);
        event.setEndSwitch(isEnd);
        event.setEndYear(endYear);
        event.setEndMonth(endMonth);
        event.setEndDay(endDay);
        event.setEndWeekday(endWeekday);
        event.setLastUpdateTime(System.currentTimeMillis());
        return event.save();
    }

    @Override
    public boolean updateEvent(int eventId, String eventTitle, int year, int month, int day, int weekday, int sortId, boolean isTop, int repeatType, boolean isEnd, int endYear, int endMonth, int endDay, int endWeekday) {
        Event event = LitePal.find(Event.class, eventId);
        if (event == null){
            return false;
        }

        event.setTitle(eventTitle);
        event.setYear(year);
        event.setMonth(month);
        event.setDay(day);
        event.setWeekDay(weekday);
        event.setSortId(sortId);
        event.setTop(isTop);
        event.setRepeatType(repeatType);
        event.setEndSwitch(isEnd);
        event.setEndYear(endYear);
        event.setEndMonth(endMonth);
        event.setEndDay(endDay);
        event.setEndWeekday(endWeekday);
        event.setLastUpdateTime(System.currentTimeMillis());

        return event.saveOrUpdate("id = " + eventId);
    }

    @Override
    public List<Event> getSortEventList(int sortId) {
        List<Event> list = LitePal.where("sortId = " + sortId).find(Event.class);
        return list;
    }

    @Override
    public void getSortEventList(final int sortId, final DataListener<List<Event>> dataListener) {
        final List<Event> list = new ArrayList<>();
        ExecutorHelper.INSTANCE.getCacheExecutor().execute(new Runnable() {
            @Override
            public void run() {
                List<Event> eventList = LitePal.where("sortId = " + sortId).find(Event.class);
                list.addAll(eventList);

                App.mainHandler.post(() -> dataListener.onSuccess(list));
            }
        });
    }

    @Override
    public List<Event> getAllEventList() {
        List<Event> list = LitePal.findAll(Event.class);
        return list;
    }

    @Override
    public void getAllEventList(final DataListener<List<Event>> dataListener) {
        final List<Event> list = new ArrayList<>();

        ExecutorHelper.INSTANCE.getCacheExecutor().execute(() -> {
            List<Event> eventList = LitePal.findAll(Event.class);
            list.addAll(eventList);

            App.mainHandler.post(() -> dataListener.onSuccess(list));
        });
    }

    @Override
    public String getSortName(int sortId) {
        String sortName = "";
        Event.Sort sort = LitePal.find(Event.Sort.class, sortId);
        if (sort != null){
            sortName = sort.getName();
        }
        return sortName;
    }

    @Override
    public Event getEvent(int eventId) {
        Event event = LitePal.find(Event.class, eventId);
        return event;
    }

    @Override
    public boolean deleteEvent(int eventId) {
        Event event = LitePal.find(Event.class, eventId);
        if (event == null){
            return true;
        }
        event.delete();
        return true;
    }

    @Override
    public void addDefaultConfig() {
        Log.d(TAG, "addDefaultConfig: ");
        Config config = new Config();

        config.setId(1);

        config.setCurrentDayRemind(1);
        config.setCurrentRemindHour(9);
        config.setCurrentRemindMin(0);

        config.setBeforeDayRemind(1);
        config.setBeforeRemindHour(9);
        config.setBeforeRemindMin(0);

        config.save();
    }

    @Override
    public Config getRemindConfigData() {
        Config config = LitePal.find(Config.class, 1);
        if (config == null){
            addDefaultConfig();
        }
        config = LitePal.find(Config.class, 1);
        return config;
    }

    @Override
    public void updateCurrentRemindSwitch(boolean value) {
        Log.d(TAG, "updateCurrentRemindSwitch: " + value);
        Config config = LitePal.find(Config.class, 1);
        if (value){
            config.setCurrentDayRemind(1);
        }else {
            config.setCurrentDayRemind(0);
        }

        boolean result = config.saveOrUpdate("id = " + config.getId());
        Log.d(TAG, "updateCurrentRemindSwitch: " + result);
    }

    @Override
    public void updateBeforeRemindSwitch(boolean value) {
        Log.d(TAG, "updateBeforeRemindSwitch: " + value);
        Config config = LitePal.find(Config.class, 1);
        if (value){
            config.setBeforeDayRemind(1);
        }else {
            config.setBeforeDayRemind(0);
        }
        boolean result = config.saveOrUpdate("id = " + config.getId());
        Log.d(TAG, "updateCurrentRemindSwitch: " + result);
    }

    @Override
    public void updateCurrentRemindTime(int hour, int min) {
        Config config = LitePal.find(Config.class, 1);
        config.setCurrentRemindHour(hour);
        config.setCurrentRemindMin(min);
        boolean result = config.saveOrUpdate("id = " + config.getId());
        Log.d(TAG, "updateCurrentRemindSwitch: " + result);
    }

    @Override
    public void updateBeforeRemindTiem(int hour, int min) {
        Config config = LitePal.find(Config.class, 1);
        config.setBeforeRemindHour(hour);
        config.setBeforeRemindMin(min);
        boolean result = config.saveOrUpdate("id = " + config.getId());
        Log.d(TAG, "updateCurrentRemindSwitch: " + result);
    }

    @Override
    public List<Event> getEventListByDate(int year, int month, int day) {
        List<Event> eventList = LitePal.where("year = ? and month = ? and day = ?",
                String.valueOf(year),
                String.valueOf(month),
                String.valueOf(day)
        ).find(Event.class);
        return eventList;
    }

    @Override
    public void getEventListByDate(final int year, final int month, final int day, final DataListener<List<Event>> dataListener) {
        final List<Event> list = new ArrayList<>();
        ExecutorHelper.INSTANCE.getCacheExecutor().execute(new Runnable() {
            @Override
            public void run() {
                List<Event> eventList = LitePal.where("year = ? and month = ? and day = ?",
                        String.valueOf(year),
                        String.valueOf(month),
                        String.valueOf(day)
                ).find(Event.class);
                list.addAll(eventList);

                App.mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        dataListener.onSuccess(list);
                    }
                });

            }
        });
    }

    @Override
    public void updateEvent(Event event) {
        event.saveOrUpdate("id = " + event.getId());
    }

    @Override
    public void saveEvent(Event event) {
        event.save();
    }

    @Override
    public void getSlidMenuSortData(final Context context, final DataListener<List<ItemSlidMenuSort>> dataListener) {
        if (context == null || dataListener == null){
            return;
        }

        //异步调用
        final List<ItemSlidMenuSort> list = new ArrayList<>();

        ExecutorHelper.INSTANCE.getCacheExecutor().execute(new Runnable() {
            @Override
            public void run() {
                //第一项为全部
                ItemSlidMenuSort firstItemSlidMenuSort = new ItemSlidMenuSort();
                firstItemSlidMenuSort.setCount(LitePal.findAll(Event.class).size());
                firstItemSlidMenuSort.setName(context.getString(R.string.dm_all));
                firstItemSlidMenuSort.setId(0);
                list.add(firstItemSlidMenuSort);

                //查询分类数
                List<Event.Sort> sortList = LitePal.findAll(Event.Sort.class);
                Log.d(TAG, "getSlidMenuSortData: sort size = " + sortList.size());

                for (Event.Sort sort: sortList){
                    ItemSlidMenuSort itemSlidMenuSort = new ItemSlidMenuSort();
                    itemSlidMenuSort.setId(sort.getId());
                    itemSlidMenuSort.setName(sort.getName());
                    Log.d(TAG, "getSlidMenuSortData: id = " + sort.getId());

                    //查询该id的事件数
                    List<Event> eventList = LitePal.where("sortId = " + sort.getId()).find(Event.class);
                    if (eventList != null){
                        itemSlidMenuSort.setCount(eventList.size());
                    }else {
                        itemSlidMenuSort.setCount(0);
                    }
                    list.add(itemSlidMenuSort);
                }

                App.mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        dataListener.onSuccess(list);
                    }
                });
            }
        });
    }

    @Override
    public void getSortData(Context context, final DataListener<List<Event.Sort>> dataListener) {
        if (context == null || dataListener == null){
            return;
        }

        //异步调用
        final List<Event.Sort> list = new ArrayList<>();

        ExecutorHelper.INSTANCE.getCacheExecutor().execute(new Runnable() {
            @Override
            public void run() {
                //查询分类数
                List<Event.Sort> sortList = LitePal.findAll(Event.Sort.class);
                Log.d(TAG, "getSortData: sort size = " + sortList.size());
                list.addAll(sortList);

                App.mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        dataListener.onSuccess(list);
                    }
                });
            }
        });
    }

    @Override
    public Event.Sort saveSort(String name) {
        Event.Sort sort = new Event.Sort();
        sort.setDefaultSort(false);
        sort.setName(name);
        sort.save();
        return sort;
    }

    @Override
    public void modifySort(int id, String name) {
        Event.Sort sort = LitePal.find(Event.Sort.class, id);
        sort.setName(name);
        sort.update(id);
    }

    @Override
    public void deleteSort(int id) {
        LitePal.delete(Event.Sort.class, id);
    }
}
