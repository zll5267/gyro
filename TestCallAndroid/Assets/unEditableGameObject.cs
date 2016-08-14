using UnityEngine;
using System.Collections;

public class unEditableGameObject : MonoBehaviour {

	// Use this for initialization
	void Start () {
		//var sphere = GameObject.CreatePrimitive(PrimitiveType.Sphere);  
		//sphere.hideFlags = HideFlags.HideInHierarchy;  
		//sphere.hideFlags = HideFlags.HideInInspector; 
		//sphere.hideFlags = HideFlags.NotEditable; 
		GameObject moveObj = GameObject.FindGameObjectWithTag("moveobj");
		moveObj.hideFlags = HideFlags.NotEditable;  
	}

}
