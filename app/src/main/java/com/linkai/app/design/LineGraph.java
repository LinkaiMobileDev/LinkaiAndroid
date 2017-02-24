package com.linkai.app.design;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.linkai.app.R;

/**
 * Created by LP1001 on 04-11-2016.
 */
public class LineGraph extends View {

    private final String TAG = "LineGraph";

    Context context;

    private Paint paintLine,paintXAxis,paintXAxisText,paintXAxisTextBG,paintGraphFill;
    private Path path;
    private Path pathFill;

    private float width=0;
    private float height=0;
    float graph_usable_height=0;
    private float x_unit=0;
    private float y_unit=0;
    float x_axis_text_span_height=80;
    private String[] x_attrs;
    private float[] values;

    public LineGraph(Context context) {
        super(context);
        this.context=context;
        init();
    }

    public LineGraph(Context context, AttributeSet attrs) {

        super(context, attrs);
        this.context=context;
        init();
    }

    public LineGraph(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        init();
    }

    private void init() {
        width=this.getWidth();
        height=this.getHeight();

        paintLine = new Paint();
        paintLine.setColor(ContextCompat.getColor(context, R.color.blue1));
        paintLine.setStrokeWidth(5);
        paintLine.setStyle(Paint.Style.STROKE);

        paintGraphFill=new Paint();
        paintGraphFill.setColor(ContextCompat.getColor(context, R.color.gray1));
        paintGraphFill.setStyle(Paint.Style.FILL);

        paintXAxis=new Paint();
        paintXAxis.setColor(Color.DKGRAY);
        paintXAxis.setStrokeWidth(2);
        paintXAxis.setStyle(Paint.Style.STROKE);

        paintXAxisText=new Paint();
        paintXAxisText.setColor(Color.DKGRAY);
        paintXAxisText.setTextSize(30);
        paintXAxisText.setTextAlign(Paint.Align.CENTER);

        paintXAxisTextBG=new Paint();
        paintXAxisTextBG.setColor(Color.WHITE);
        paintXAxis.setStyle(Paint.Style.FILL_AND_STROKE);


        path = new Path();
        pathFill=new Path();


        Log.d(TAG, "init: "+width+","+height);

    }

    public void setAttrAndValues(String[] attrs_x,float[] _values){
        x_attrs=attrs_x;
        this.values=_values;

        invalidate();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(x_attrs==null || values==null || x_attrs.length==0 || values.length==0 || x_attrs.length!=values.length){
//            path.moveTo(0,graph_usable_height);
//            path.lineTo(width,graph_usable_height);
//            canvas.drawPath(path,paintLine);
            return;
        }
        width=this.getWidth();
        height=this.getHeight();
        graph_usable_height=height-x_axis_text_span_height;
//        calculating unit in x-axis and y-axis
        x_unit=width/(x_attrs.length+1);
        y_unit=calcYUnit();
        //        drawing x axis text
        for(int i=0;i<x_attrs.length;i++){
            canvas.drawText(x_attrs[i],(x_unit*(i+1)),height-40,paintXAxisText);
            canvas.drawText("2016",(x_unit*(i+1)),height-10,paintXAxisText);
        }
//        plot values
        path.moveTo(0,height/2);
        pathFill.moveTo(0,height/2);
        for(int i=0;i<values.length;i++){
            Log.d(TAG, "onDraw: unit="+y_unit+", value="+values[i]);
            path.lineTo(x_unit*(i+1),graph_usable_height-(y_unit*values[i]));
            pathFill.lineTo(x_unit*(i+1),graph_usable_height-(y_unit*values[i]));
        }
        path.lineTo(width,graph_usable_height/2);
        pathFill.lineTo(width,graph_usable_height/2);
//        to shade under the graph line
        pathFill.lineTo(width,graph_usable_height);
        pathFill.lineTo(0,graph_usable_height);
        pathFill.moveTo(0,height/2);
        canvas.drawPath(pathFill,paintGraphFill);
//        draw line outer to shade
        canvas.drawPath(path,paintLine);
    }

    private float calcYUnit(){
        float unit=0;
        float largest_value=0;
        float smallest_value=values.length>0?values[0]:0;
//        finding largest value in array
        for(int i=0;i<values.length;i++){
            if(values[i]>largest_value){
                largest_value=values[i];
            }
            if(values[i]<smallest_value){
                smallest_value=values[i];
            }
        }

//        if(largest_value>height){
            unit=graph_usable_height/largest_value;
        Log.d(TAG, "calcYUnit: "+height);
//        }
        return unit;
    }



}
