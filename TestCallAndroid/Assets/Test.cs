using UnityEngine;
using System.Collections;

public class Test : MonoBehaviour
{
	public GameObject cube;
	private float deltaGyro = 0;
	private Vector3 cubecurrentposition;

	// Update is called once per frame
	void Update ()
	{
		//当用户按下手机的返回键或home键退出游戏
		if (Input.GetKeyDown(KeyCode.Escape) || Input.GetKeyDown(KeyCode.Home) )
		{
			Application.Quit();
		}
		deltaGyro -= GyroxAndroidInput();

		cubecurrentposition = cube.transform.localPosition;
		cubecurrentposition.x = cube.transform.position.x + deltaGyro;
		cube.transform.position = cubecurrentposition;

	}
		
	private float GyroxAndroidInput(){
		AndroidJavaClass jc = new AndroidJavaClass ("com.unity3d.player.UnityPlayer");
		AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject> ("currentActivity");
		float mGyroxInput = jo.Call<float> ("getSensorInfoX");
		return mGyroxInput;
	}
}
