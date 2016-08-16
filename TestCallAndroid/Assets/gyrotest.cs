using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class gyrotest : MonoBehaviour {
	public  InputField ip;
	public  InputField port;
	public  InputField backup;
	public  Text text;
	private bool connect = false;
	private float oldValue = 0;
	private float newValue = 0;
	private float minValue = 0;
	private float maxValue = 0;
	private const float maxY = 170;
	private const float minY = 140;
	private int flag = 1;

	public GameObject cube;
	private Vector3 cubecurrentposition;

    AndroidJavaObject mJO;

    private float getSensorInfoX() {
        //AndroidJavaClass jc = new AndroidJavaClass ("com.unity3d.player.UnityPlayer");
        //AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject> ("currentActivity");

        float gyrox = mJO.Call<float> ("getSensorInfoX");
        return gyrox;
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
            float mGyrox = getSensorInfoX ();
			backup.text = mGyrox.ToString ();
			oldValue = newValue;
			newValue = mGyrox;
			float delta = newValue - oldValue;
			if (delta > maxValue) {
				maxValue = delta;
			} else if (delta < minValue) {
				minValue = delta;
			}
			//string maxs = maxValue.ToString ();
			//string minx = minValue.ToString ();
			//text.text = "max:" +  maxs + ", min:" + minx;


			if (delta > 0.1 || delta < -0.1) {
				text.text = delta.ToString();
				cubecurrentposition = cube.transform.position;
				cubecurrentposition.y = cube.transform.position.y + delta * flag;
				if (cubecurrentposition.y > maxY) {
					cubecurrentposition.y = maxY;
					flag = flag * -1;
				} else if (cubecurrentposition.y < minY) {
					cubecurrentposition.y = minY;
					flag = flag * -1;
				}
				cube.transform.position = cubecurrentposition;
			}
		}
	}

	public void getGyroxInUnity(){
		
        float mGyrox = getSensorInfoX ();
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
