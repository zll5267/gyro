using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class gyrotest : MonoBehaviour {
	public  InputField ip;
	public  InputField port;
	public  InputField backup;
	public  Text text;
	private bool connect = false;
	private float oldValueX = 0;
	private float newValueX = 0;
	private float oldValueY = 0;
	private float newValueY = 0;
	//private float minValue = 0;
	//private float maxValue = 0;
	private const float maxY = 162;
	private const float minY = 145;
	private const float maxX = 307;
	private const float minX = 297;
	private float flag = 1.5f;
	private float mGyrox = 0;
	private float mGyroy = 0;
	//private float mGyroz = 0;
	private float mAccx = 0;
	private float mAccy = 0;

	public GameObject cube;
	private Vector3 cubecurrentposition;

    AndroidJavaObject mJO;

    private float getGyroSensorInfoX() {
		float gyrox = mJO.Call<float> ("getGyroSensorInfoX");
        return gyrox;
    }

	private float getGyroSensorInfoY() {
		float gyroy = mJO.Call<float> ("getGyroSensorInfoY");
		return gyroy;
	}

	private float getGyroSensorInfoZ() {
		float gyroz = mJO.Call<float> ("getGyroSensorInfoZ");
		return gyroz ;
	}

	private float getAccSensorInfoX() {
		float accx = mJO.Call<float> ("getAccSensorInfoX");
		return accx - 0.4f;
	}

	private float getAccSensorInfoY() {
		//AndroidJavaClass jc = new AndroidJavaClass ("com.unity3d.player.UnityPlayer");
		//AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject> ("currentActivity");

		float accy = mJO.Call<float> ("getAccSensorInfoY");
		return accy - 0.2f;
	}

	private void accMouse(){
		mAccx = getAccSensorInfoX ();
		mAccy = getAccSensorInfoY ();

		oldValueX = newValueX;
		newValueX = mAccx;
		oldValueY = newValueY;
		newValueY = mAccy;
		float deltaX = newValueX - oldValueX;
		float deltaY = newValueY - oldValueY;
		cubecurrentposition = cube.transform.position;

		if ( (mAccx > 0.2 || mAccx < -0.2) && (deltaX != 0)) {
			print ("mAccx is"+mAccx);
			cubecurrentposition.x = cube.transform.position.x + mAccx * flag;
			if (cubecurrentposition.x > maxX) {
				cubecurrentposition.x = maxX;
			} else if (cubecurrentposition.x < minX) {
				cubecurrentposition.x = minX;
			}
			print ("Cube position x is"+cubecurrentposition.x);
		}

		if ((mAccy > 0.2 || mAccy < -0.2) && (deltaY != 0)) {
			//print ("mAccy is"+mAccy);
			cubecurrentposition.y = cube.transform.position.y + mAccy * flag;
			if (cubecurrentposition.y > maxY) {
				cubecurrentposition.y = maxY;
			} else if (cubecurrentposition.y < minY) {
				cubecurrentposition.y = minY;
			}
			//print ("Cube position y is"+cubecurrentposition.y);
		}

		cube.transform.position = cubecurrentposition;
	}

	private void gyroMouse(){
		
		mGyrox = -getGyroSensorInfoZ ();
		mGyroy = getGyroSensorInfoX ();
		oldValueX = newValueX;
		newValueX = mGyrox;
		oldValueY = newValueY;
		newValueY = mGyroy;
		float deltaX = newValueX - oldValueX;
		float deltaY = newValueY - oldValueY;

		cubecurrentposition = cube.transform.position;
	
		if ( (mGyrox > 0.1 || mGyrox < -0.1) && (deltaX != 0)) {
			print ("mGyrox is"+mGyrox);
			cubecurrentposition.x = cube.transform.position.x + mGyrox * flag;
			if (cubecurrentposition.x > maxX) {
				cubecurrentposition.x = maxX;
			} else if (cubecurrentposition.x < minX) {
				cubecurrentposition.x = minX;
			}
			print ("Cube position x is"+cubecurrentposition.x);
		}

		if ((mGyroy > 0.1 || mGyroy < -0.1) && (deltaY != 0)) {
			//print ("mGyroy is"+mGyroy);
			cubecurrentposition.y = cube.transform.position.y + mGyroy * flag;
			if (cubecurrentposition.y > maxY) {
				cubecurrentposition.y = maxY;
			} else if (cubecurrentposition.y < minY) {
				cubecurrentposition.y = minY;
			}
			//print ("Cube position y is"+cubecurrentposition.y);
		}

		cube.transform.position = cubecurrentposition;
	}

    void Start() {
        mJO = new AndroidJavaObject("com.example.koufula.gyrowififorunity.Main");
    }

	// Update is called once per frame
	void Update () {
		//当用户按下手机的返回键或home键退出游戏
		if (Input.GetKeyDown(KeyCode.Escape) || Input.GetKeyDown(KeyCode.Home) )
		{
			Application.Quit();
		}
		if (connect) {
			gyroMouse ();
		}
	}

	public void getGyroxInUnity(){
		
        float mGyrox = getGyroSensorInfoX ();
		float fe1 = 100;
		text.text = fe1.ToString();
		//backup.text = fe1.ToString ();
		//text.text = mGyrox.ToString ();
		backup.text = mGyrox.ToString ();

	}
	public void setIpPorttoAndroid(){
		//AndroidJavaClass jc = new AndroidJavaClass ("com.unity3d.player.UnityPlayer");
		//AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject> ("currentActivity");
        //AndroidJavaObject jo = new AndroidJavaObject("com.example.koufula.gyrowififorunity.Main");
		mJO.Call("startDataReceiveThread",ip.text,port.text);
		connect = true;
	}
}
