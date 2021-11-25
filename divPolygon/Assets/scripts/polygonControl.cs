using UnityEngine;
using System.Collections;
using System.Collections.Generic;

public class polygonControl : MonoBehaviour {
	public Color m_edgeColor = Color.white;
	public Color m_faceColor = Color.white;
	public bool m_isSolid=false;
	void Start(){
		m_faceColor=new Color(Random.Range(0f,1f),Random.Range(0f,1f),Random.Range(0f,1f),1);
	}
	void Update(){
		for (int i = 0; i < transform.childCount; i++) {
			Transform child=transform.GetChild(i);
			Vector3 vPos = child.position;
			vPos.z = 0;
			child.position = vPos;
		}
	}
	public void createPolygon(List<Vector2> vlist){
		
		for (int i = 0; i < vlist.Count; i++) {
			Vector2 v = vlist [i];
			Transform child=null;
			if(i<transform.childCount){
				child=transform.GetChild(i);
			}else{
				GameObject geo= new GameObject ("v");
				geo.transform.parent = transform;
				child = geo.transform;
			}
			child.position = new Vector3 (v.x,v.y,0);

		}
		for (int i = transform.childCount-1; i >= vlist.Count; i--) {
			Transform child=transform.GetChild(i);
			DestroyImmediate (child.gameObject);
		}

	
	}
	public List<Vector2> getVlist(){
		List<Vector2> vlist = new List<Vector2> ();
		for (int i = 0; i < transform.childCount; i++) {
			Transform child=transform.GetChild(i);
			Vector2 v = funcs.v3ToV2 (child.position);
			vlist.Add (v);
		}
		return vlist;
	}
	void OnDrawGizmos(){

		//----draw polygon
		//draw outline
		Gizmos.color = m_edgeColor;
		for (int i = 0; i < transform.childCount; i++) {
			Transform child=transform.GetChild(i);
			Transform childn = transform.GetChild ((i+1)%transform.childCount);
			Vector2 v = funcs.v3ToV2(child.position);
			Vector2 vn = funcs.v3ToV2(childn.position);
			Gizmos.DrawLine (v,vn);
		}
		//draw solid
		if (m_isSolid) {
			Gizmos.color = m_faceColor;
			List<Vector2> vlist = new List<Vector2> ();
			for (int i = 0; i < transform.childCount; i++) {
				Transform child = transform.GetChild (i);
				Vector2 v = funcs.v3ToV2 (child.position);
				vlist.Add (v);
			}
			Triangulator trianglulator = new Triangulator (vlist);
			int[] indices = trianglulator.Triangulate ();
			// Create the Vector3 vertices
			Vector3[] vertices = new Vector3[vlist.Count];
			for (int i = 0; i < vertices.Length; i++) {
				vertices [i] = new Vector3 (vlist [i].x, vlist [i].y, 0);
			}

			// Create the mesh
			Mesh msh = new Mesh ();
			msh.vertices = vertices;
			msh.triangles = indices;
			msh.RecalculateNormals ();
			msh.RecalculateBounds ();
			Gizmos.DrawMesh (msh);
		}

	
	}
}
