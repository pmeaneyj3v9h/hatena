using UnityEngine;
using System.Collections;
using System.Collections.Generic;
public class divResultControl : MonoBehaviour {

	public polygonControl m_polygonControl;
	public lineControl m_lineControl;

	void Awake(){
		
	}
	void Update () {
		if (m_lineControl.isShrinkToDot () == false) {
			List<List<Vector2> > subPolygons = new List<List<Vector2> > ();
			Vector2 start = funcs.v3ToV2 (m_lineControl.m_start.position);
			Vector2 end = funcs.v3ToV2 (m_lineControl.m_end.position);
			Vector2 veryFarStartPoint = funcs.v3ToV2 (m_lineControl.m_veryFarStartPoint);
			subPolygons=funcs.doLineDivPolygon (m_polygonControl.getVlist (), start, end,veryFarStartPoint);
		
			for (int i = 0; i < subPolygons.Count; i++) {
				List<Vector2> subPolygon = subPolygons [i];
				Transform child = null;
				if (i < transform.childCount) {
					child = transform.GetChild (i);


				} else {
					GameObject subPolygonNode = new GameObject ("subPolygon");
					subPolygonNode.transform.parent = transform;
					polygonControl polygonControl=subPolygonNode.AddComponent<polygonControl> ();
					polygonControl.m_isSolid = true;
					polygonControl.m_edgeColor = Color.white;

					child = polygonControl.transform;
				}
				child.GetComponent<polygonControl>().createPolygon (subPolygon);

			}
			for (int i = transform.childCount-1; i >= subPolygons.Count; i--) {
				Transform child=transform.GetChild(i);
				DestroyImmediate (child.gameObject);
			}
		}

	}
}
