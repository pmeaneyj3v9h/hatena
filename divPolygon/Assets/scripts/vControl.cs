using UnityEngine;
using System.Collections;

public class vControl : MonoBehaviour {
	public float m_size=0.2f;
	public Color m_color=Color.green;
	void OnDrawGizmos(){
		Gizmos.color = m_color;
		Gizmos.DrawCube (transform.position,Vector3.one*m_size);
	}
}
