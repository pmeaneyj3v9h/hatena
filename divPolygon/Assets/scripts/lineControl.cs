using UnityEngine;
using System.Collections;

public class lineControl : MonoBehaviour {

	public Transform m_start;
	public Transform m_end;
	public Vector3 m_veryFarStartPoint;
	void Awake(){
		m_start = transform.FindChild ("start");
		m_end = transform.FindChild ("end");
		updateVerFarStartPoint ();
	}
	public bool isShrinkToDot(){
		if (Vector2.Distance (funcs.v3ToV2(m_start.position), funcs.v3ToV2(m_end.position)) == 0) {
			return true;
		
		} else {
			return false;
		}
	}
	void Update(){
		updateVerFarStartPoint ();
	}
	void updateVerFarStartPoint(){
		m_veryFarStartPoint = m_start.position + (m_start.position - m_end.position).normalized * 10000f;
	}
	void OnDrawGizmos(){
		Gizmos.color = Color.yellow;
		Gizmos.DrawLine (m_start.position, m_end.position);
		Gizmos.color = Color.gray;
		Gizmos.DrawLine (m_start.position, m_veryFarStartPoint);
	}
}
