package at.smartlab.html5cam;

import java.io.IOException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.IBinder;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import at.smartlab.html5cam.R;
import at.smartlab.html5cam.httpservice.LocalHttpService;

public class HTML5CamActivity extends Activity implements PreviewCallback, Callback {
	
	private LocalHttpService mBoundService;
	
	private boolean mIsBound;
	
	final Camera camera = Camera.open();

	private SurfaceHolder previewHolder;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        SharedPreferences settings = getSharedPreferences("CAMPREFS", 0);
        LocalHttpService.setPwd(settings.getString("pwd", ""));
        
        final ImageButton startButton = (ImageButton) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	doBindService();
            }
        });
        final ImageButton stopButton = (ImageButton) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	doUnbindService();
            }
        });
        
        final Button storeButton = (Button) findViewById(R.id.saveButton);
        storeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	SharedPreferences settings = getSharedPreferences("CAMPREFS", 0);
            	final EditText et1 = (EditText) findViewById(R.id.password);
            	final EditText et2 = (EditText) findViewById(R.id.retype);
            	if(et1.getText().toString().equals(et2.getText().toString())) {
            		Toast.makeText(getApplicationContext(), "Stored", Toast.LENGTH_SHORT).show();
            		String pwd = et1.getText().toString();
            		LocalHttpService.setPwd(pwd);
            		Editor ed = settings.edit();
            		ed.putString("pwd", pwd);
            		ed.commit();
            	}
            }
        });
        
        
        
        final SurfaceView preview = (SurfaceView) findViewById(R.id.preView);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        camera.setPreviewCallback(this);
    }
    
    private void initPreview(int width, int height) {
    	try {
			camera.setPreviewDisplay(previewHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
         
    }
      
      private void startPreview() {
    	  Parameters params = camera.getParameters();
          params.setPreviewFrameRate(1);
          camera.setParameters(params);
          camera.startPreview();
      }
    
    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
          // no-op -- wait until surfaceChanged()
        }
        
        public void surfaceChanged(SurfaceHolder holder,
                                   int format, int width,
                                   int height) {
          initPreview(width, height);
          startPreview();
        }
        
        public void surfaceDestroyed(SurfaceHolder holder) {
          // no-op
        }
      };
    
    public void onPreviewFrame(byte[] data, Camera camera) {
    	
    	Camera.Parameters parameters = camera.getParameters();
    	
        Size size = parameters.getPreviewSize();
        
        //Log.e("NXTBotGuard", "" + size.width + " " + size.height);
        
        final YuvImage image = new YuvImage(data, parameters.getPreviewFormat(),
                size.width, size.height, null);
        
        mBoundService.setImage(image);
        /*
        File file = new File(Environment.getExternalStorageDirectory()
                .getPath() + "/out.jpg");
        FileOutputStream filecon = new FileOutputStream(file);
        image.compressToJpeg(
                new Rect(0, 0, image.getWidth(), image.getHeight()), 90,
                filecon);
		*/
	}

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((LocalHttpService.LocalBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
        }
    };
	

    void doBindService() {
    	Intent mServiceIntent = new Intent(this, LocalHttpService.class);
        bindService(mServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        camera.setPreviewCallback(null);
        camera.release();
        doUnbindService();
    	
    	super.onDestroy();
    }

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}
}