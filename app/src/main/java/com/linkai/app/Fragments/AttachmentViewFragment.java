package com.linkai.app.Fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linkai.app.R;
import com.linkai.app.libraries.Const;

/**
 * A simple {@link Fragment} subclass.
 */
public class AttachmentViewFragment extends BottomSheetDialogFragment implements View.OnClickListener{
    private final String TAG="AttachmentViewFragment";

    Context context;
    AttachmentViewFragment current_instance;
    Dialog dialog;

    private View llAttachGallery;
    private View llAttachVideo;
    private View llAttachAudio;
    private int RESULT_LOAD_FILE=1;
    AttachmentSelectedListener attachmentListener=null;


    public AttachmentViewFragment() {
        // Required empty public constructor
    }



    public interface AttachmentSelectedListener{
        void imageSelected(String path);
        void videoSelected(String path);
        void audioSelected(String path);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context=this.getActivity().getApplicationContext();
        current_instance=this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_attachment_view, container, false);
        llAttachGallery=view.findViewById(R.id.llAttachGallery);
        llAttachGallery.setOnClickListener(this);
        llAttachVideo=view.findViewById(R.id.llAttachVideo);
        llAttachVideo.setOnClickListener(this);
        llAttachAudio=view.findViewById(R.id.llAttachAudio);
        llAttachAudio.setOnClickListener(this);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog= super.onCreateDialog(savedInstanceState);
        return dialog;
    }

    public void setAttachmentListener(AttachmentSelectedListener _attachmentListener) {

        try {
            attachmentListener = (AttachmentSelectedListener) _attachmentListener;
        } catch (ClassCastException e) {
            throw new ClassCastException( " must implement AttachmentSelectedListener");
        }
    }

    @Override
    public void onClick(View view) {
        Intent browse_file_intent;
        String[] extra_mime_types;
        switch (view.getId()){
            case R.id.llAttachGallery:
                browse_file_intent=new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                browse_file_intent.setType("image/*");
                extra_mime_types=new String[]{"image/jpeg","image/png"};
                browse_file_intent.putExtra(Intent.EXTRA_MIME_TYPES,extra_mime_types);
                startActivityForResult(browse_file_intent,Const.ATTACHMENT_TYPE.IMAGE.toInteger());
                break;
            case R.id.llAttachVideo:
                browse_file_intent=new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                browse_file_intent.setType("video/*");
                extra_mime_types=new String[]{"video/mp4","video/3gpp","video/3gpp2","video/avi"};
                browse_file_intent.putExtra(Intent.EXTRA_MIME_TYPES,extra_mime_types);
                startActivityForResult(browse_file_intent,Const.ATTACHMENT_TYPE.VIDEO.toInteger());
                break;
            case R.id.llAttachAudio:
                Log.d(TAG, "onClick: audio");
                browse_file_intent=new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
//                browse_file_intent.setType("audio/*");
//                extra_mime_types=new String[]{};
//                browse_file_intent.putExtra(Intent.EXTRA_MIME_TYPES,extra_mime_types);
                startActivityForResult(browse_file_intent,Const.ATTACHMENT_TYPE.AUDIO.toInteger());
                break;
            default:break;
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: "+resultCode+" "+requestCode+" ");
        super.onActivityResult(requestCode, resultCode, data);
        //Toast.makeText(context,"image selected",Toast.LENGTH_SHORT).show();
        //Log.d("SingleChat", "onActivityResult: ");
        try {
            // When an Image is picked
            if (resultCode == getActivity().RESULT_OK && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                Log.d(TAG, "onActivityResult: uri-"+selectedImage);
                String[] filePathColumn = { MediaStore.Images.Media.DATA,MediaStore.Video.Media.DATA };

                // Get the cursor
                Cursor cursor = context.getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();
                String path = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                cursor.close();
                if(attachmentListener!=null){
                    if (requestCode == Const.ATTACHMENT_TYPE.IMAGE.toInteger()){
                        attachmentListener.imageSelected(path);
                    }
                    else if (requestCode == Const.ATTACHMENT_TYPE.VIDEO.toInteger()){
                        attachmentListener.videoSelected(path);
                    }
                    else if (requestCode == Const.ATTACHMENT_TYPE.AUDIO.toInteger()){
                        attachmentListener.audioSelected(path);
                    }

                }
                else{
                    throw new NullPointerException("Attachment Listener is not set");
                }

////                sending file
//                fileHandler.sendImage(imgDecodableString,chatId, Const.CHATBOX_TYPE.SINGLE);
////                refresing listview
//                loadChatBoxMessages();
                //Log.d("singlechat", "onActivityResult: filename");

            } else {
                //Toast.makeText(this, "You haven't picked Image",Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            //Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG) .show();
            //Log.d("SinglrChat", "onActivityResult: "+e.getMessage());
        }
        dialog.dismiss();
    }



}
