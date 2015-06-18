package net.bychawski.guardian.guardian;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.bychawski.guardian.guardian.models.Item;
import net.bychawski.guardian.guardian.rest_client.RESTClientManager;
import net.bychawski.guardian.guardian.util.AppEvent;
import net.bychawski.guardian.guardian.util.AppEventListener;
import net.bychawski.guardian.guardian.util.AppEventType;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;

public class ItemsListFragment extends Fragment implements AdapterView.OnItemClickListener {
    private Vector<Item> _itemsCollection;
    private Activity _parentActivity;

    public static ItemsListFragment newInstance(Vector<Item> itemsCollection) {
        ItemsListFragment f = new ItemsListFragment();
        f.set_itemsCollection(itemsCollection);

        return f;
    }

    public ItemsListFragment() {
        super();
    }

    public void set_itemsCollection(Vector<Item> itemsCollection ){
        this._itemsCollection = itemsCollection;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_items_list, container, false);

        ItemsListAdapter adapter = new ItemsListAdapter(inflater, _itemsCollection, getActivity());
        ListView listView = (ListView) rootView.findViewById(R.id.items_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new MyMultiChoseModeListener(adapter));
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(getActivity(), ItemDetailsActivity.class);
        intent.putExtra(ItemDetailsActivity.EXTRA_MODE, ItemDetailsActivity.Mode.ITEM_DETAILS);
        intent.putExtra(ItemDetailsActivity.EXTRA_MODEL_ID, _itemsCollection.get(i).getId().toString());
        getActivity().startActivity(intent);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    public static class ItemsListAdapter extends BaseAdapter implements AppEventListener {

        private LayoutInflater _inflater;
        private Vector<Item> _itemsCollection;
        private HashMap<Integer,Boolean> _selections = new HashMap<Integer, Boolean>();
        private Activity _parentActivity;

        public ItemsListAdapter(LayoutInflater inflater, Vector<Item> itemsCollection, Activity parentActivity) {
            _inflater = inflater;
            _itemsCollection = itemsCollection;
            _parentActivity = parentActivity;

            GuardianApp.getInstance().addAppEventListener(this);
        }

        @Override
        public int getCount() {
            return _itemsCollection.size();
        }

        @Override
        public Object getItem(int i) {
            return _itemsCollection.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null){
                view = _inflater.inflate(R.layout.items_list_row, viewGroup, false);
            }

            TextView title = (TextView) view.findViewById(R.id.list_row_title);
            TextView subtitle = (TextView) view.findViewById(R.id.list_row_subtitle);
            TextView statusText = (TextView) view.findViewById(R.id.text_status);
            TextView localizationText = (TextView) view.findViewById(R.id.text_localization);
            ImageView categoryIcon = (ImageView) view.findViewById(R.id.list_row_icon);
            ImageView statusIcon = (ImageView) view.findViewById(R.id.status_icon);

            Item model = _itemsCollection.get(i);
            title.setText(model.getName());
            subtitle.setText(model.getCategory().toString());
            statusText.setText(model.getStatus().toString());
            localizationText.setText(GuardianApp.getInstance().getUsersCollection().getUserById(model.getLocalizationId()).getName());
            categoryIcon.setImageResource(model.getCategory().getIconId());
            statusIcon.setImageResource(model.getStatus().getIconId());

            if( _selections.get(i) != null ) {
                view.setBackgroundColor(_parentActivity.getResources().getColor(android.R.color.holo_blue_dark));
            }
            else {
                view.setBackgroundColor(_parentActivity.getResources().getColor(android.R.color.background_light));
            }
            return view;
        }

        public void removeSelection(int position) {
            _selections.remove(position);
            notifyDataSetChanged();
        }

        public void setSelection(int position) {
            _selections.put(position, true);
            notifyDataSetChanged();
        }

        public void clearSelection() {
            _selections.clear();
            notifyDataSetChanged();
        }

        public void removeSelectedItems() {
            final HashMap<Integer,Boolean> selections = new HashMap<Integer, Boolean>(_selections);
            new AlertDialog.Builder(_parentActivity)
                    .setTitle(R.string.confirm_multiple_delete_dialog_title)
                    .setMessage(R.string.confirm_multiple_delete_dialog_message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Vector<Integer> itemsToRemove = new Vector<Integer>(selections.keySet());
                            Collections.sort(itemsToRemove);
                            for (int j = itemsToRemove.size() - 1; j >= 0; --j) {
                                Item item = _itemsCollection.get( itemsToRemove.elementAt(j) );
                                GuardianApp.getInstance().getItemsCollection().deleteItem(item);
                                if( GuardianApp.getInstance().getLoggedUser().getId().equals(item.getOwnerId()) ) {
                                    RESTClientManager.getInstance().deleteItem(item.getId());
                                }
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //do nothing
                        }
                    })
                    .show();
        }

        @Override
        public void onAppEvent(AppEvent event) {
            if (event.getType() == AppEventType.ITEMS_COLLECTION_CHANGED || event.getType() == AppEventType.USERS_COLLECTION_CHANGED) {
                notifyDataSetChanged();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    public static class MyMultiChoseModeListener implements AbsListView.MultiChoiceModeListener {
        private ItemsListAdapter mAdapter;
        private int mCounter = 0;

        public MyMultiChoseModeListener(ItemsListAdapter adapter){
            mAdapter = adapter;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode actionMode, int position, long l, boolean checked) {
            if (checked) {
                mAdapter.setSelection(position);
                mCounter++;
            } else {
                mAdapter.removeSelection(position);
                mCounter--;
            }
            actionMode.setTitle(mCounter+" selected");
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.context_items, menu);
            mCounter = 0;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.menu_delete:
                    mAdapter.removeSelectedItems();
                    actionMode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mAdapter.clearSelection();
        }
    }
}