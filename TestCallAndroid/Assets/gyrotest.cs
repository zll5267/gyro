using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class gyrotest : MonoBehaviour {
	public  InputField ip;
	public  InputField port;
	public  InputField backup;
	public  Text text;

	// Update is called once per frame
	void Update () {
		//当用户按下手机的返回键或home键退出游戏
		if (Input.GetKeyDown(KeyCode.Escape) || Input.GetKeyDown(KeyCode.Home) )
		{
			Application.Quit();
		}
	}
	public void getGyroxInUnity(){
		
		AndroidJavaClass jc = new AndroidJavaClass ("com.unity3d.player.UnityPlayer");
		AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject> ("currentActivity");
		float mGyrox = jo.Call<float> ("getSensorInfoX");
		float fe1 = 100;
		text.text = fe1.ToString();
		//backup.text = fe1.ToString ();
		//text.text = mGyrox.ToString ();
		backup.text = mGyrox.ToString ();
	}
	public void setIpPorttoAndroid(){
		AndroidJavaClass jc = new AndroidJavaClass ("com.unity3d.player.UnityPlayer");
		AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject> ("currentActivity");
		jo.Call("startDataReceiveThread",ip.text,port.text);
	}
}
