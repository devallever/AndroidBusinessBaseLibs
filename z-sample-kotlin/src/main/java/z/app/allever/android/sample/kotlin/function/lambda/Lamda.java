package z.app.allever.android.sample.kotlin.function.lambda;

import android.view.View;

public class Lamda {

    public void setOnClickListener(OnClickListener listener) {

    }

    public void setOnLongClickListener(OnLongClickListener listener) {

    }

    public void setOnItemClickListener(OnItemClickListener listener) {

    }

    public void setOnClickListener() {
    }



    interface OnClickListener {
        void onClick();
    }

    interface OnLongClickListener {
        void onClick(View view);
    }

    interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
