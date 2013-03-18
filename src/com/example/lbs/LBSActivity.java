package com.example.lbs;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;

public class LBSActivity extends MapActivity {
	MapView mapView;
	MapController mc;
	GeoPoint p;
	
	LocationManager lm;
	LocationListener locationListener;
	
	private class MapOverlay extends com.google.android.maps.Overlay {
		public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
			super.draw(canvas, mapView, shadow);
			
			Point screenPts = new Point();
			mapView.getProjection().toPixels(p, screenPts);
			
			Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pushpin);
			canvas.drawBitmap(bmp, screenPts.x, screenPts.y-50, null);
			return true;
		}
		
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			if(event.getAction() == 1) {
				GeoPoint p = mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
				/*Toast.makeText(getBaseContext(), "Location: " + p.getLatitudeE6() / 1E6 + "," + p.getLongitudeE6() / 1E6, Toast.LENGTH_SHORT).show();*/
			
				Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
			
				try {
					List<Address> addresses = geoCoder.getFromLocation(p.getLatitudeE6() / 1E6, p.getLongitudeE6() / 1E6, 1);
					String add = "";
					if(addresses.size() > 0) {
						for(int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++)
							add += addresses.get(0).getAddressLine(i) + "\n";
					}
					Toast.makeText(getBaseContext(), add, Toast.LENGTH_SHORT).show();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}
		return false;
		}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(true);
        mapView.setSatellite(true);
        mapView.setTraffic(true);
        
        mc = mapView.getController();
        
        /*String coordinates[] = {"1.352566007", "103.78921587"};
        double lat = Double.parseDouble(coordinates[0]);
        double lng = Double.parseDouble(coordinates[1]);
        
        p = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
        
        mc.animateTo(p);
        mc.setZoom(13);*/
        
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        try {
        	List<Address> addresses = geoCoder.getFromLocationName("empire state building", 5);
        	
        	if(addresses.size() > 0) {
        		p = new GeoPoint((int) (addresses.get(0).getLatitude() * 1E6), (int) (addresses.get(0).getLongitude() * 1E6));
        		mc.animateTo(p);
        		mc.setZoom(20);
        	}
        }
        catch (IOException e) {
        	e.printStackTrace();
        }
        
        MapOverlay mapOverlay = new MapOverlay();
        List<Overlay> listOfOverlays = mapView.getOverlays();
        listOfOverlays.clear();
        listOfOverlays.add(mapOverlay);
        
        mapView.invalidate();
        
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://www.amazon.com")), 0);
        lm.addProximityAlert(37.422006, -122.084095, 5, -1, pendingIntent);
        lm.addProximityAlert(47.422006, -112.084095, 5, -1, pendingIntent);
        
        locationListener = new MyLocationListener();
    }
    
    public void onResume() {
    	super.onResume();
    	
    	lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
    
    public void onPause() {
    	super.onPause();
    	
    	lm.removeUpdates(locationListener);
    }
    
    private class MyLocationListener implements LocationListener {
    	public void onLocationChanged(Location loc) {
    		if(loc != null) {
    			Toast.makeText(getBaseContext(), "Location changed : Lat: " + loc.getLatitude() + " Lng: " + loc.getLongitude(), Toast.LENGTH_SHORT).show();
    			
    			p = new GeoPoint((int) (loc.getLatitude() * 1E6), (int) (loc.getLongitude() * 1E6));
    			
    			mc.animateTo(p);
    			mc.setZoom(18);
    		}
    	}
    	
    	public void onProviderDisabled(String provider) {	
    	}
    	
    	public void onProviderEnabled(String provider) {
    	}
    	
    	public void onStatusChanged(String provider, int status, Bundle extras) {
    	}
    }
    
    protected boolean isRouteDisplayed() {
    	return false;
    }
}