//by wantnon， first created in 2017-2-10

using UnityEngine;
using System.Collections;
using System.Collections.Generic;

public class funcs {

	public class CvEx{
		public Vector2 m_pos;
		public float m_signedDisToDivLine=0;
		public int m_ID=0;
		public CvEx(Vector2 pos,float signedDisToDivLine,int ID){
			m_pos=pos;
			m_signedDisToDivLine=signedDisToDivLine;
			m_ID=ID;
			
		}
		public CvEx copy(){
			CvEx newVEx = new CvEx (this.m_pos,this.m_signedDisToDivLine,this.m_ID);
			return newVEx;
		}
	};
	class CsegEx{
		public CvEx m_start;
		public CvEx m_end;
		public float m_signedDisFromMiddlePointToDivLine = 0;
		public CsegEx copy(){
			CsegEx newSeg = new CsegEx ();
			newSeg.m_start = this.m_start.copy ();
			newSeg.m_end = this.m_end.copy ();
			newSeg.m_signedDisFromMiddlePointToDivLine = this.m_signedDisFromMiddlePointToDivLine;
			return newSeg;
		}

	};
	class Cseg{
		public Vector2 m_start;
		public Vector2 m_end;
		public Cseg(Vector2 start,Vector2 end){
			m_start=start;
			m_end=end;
		}
	};
	public class CboundBox{
		public float m_xmin=0;
		public float m_xmax=0;
		public float m_ymin=0;
		public float m_ymax=0;
		public Vector2 getSize(){
			Vector2 size = new Vector2 (Mathf.Abs(m_xmax-m_xmin),Mathf.Abs(m_ymax-m_ymin));
			return size;
		}
		public void init(List<Vector2> vlist){
			m_xmin = float.MaxValue;
			m_xmax = float.MinValue;
			m_ymin = float.MaxValue;
			m_ymax = float.MinValue;
			for (int i = 0; i < vlist.Count; i++) {
				Vector2 v = vlist [i];
				if (v.x < m_xmin) {
					m_xmin = v.x;
				}
				if (v.x > m_xmax) {
					m_xmax = v.x;
				}
				if (v.y < m_ymin) {
					m_ymin = v.y;
				}
				if (v.y > m_ymax) {
					m_ymax = v.y;
				}
			}//got m_xmin,m_xmax,m_ymin,m_ymax
		}
		public void extend(Vector2 v){
			if (v.x < m_xmin) {
				m_xmin = v.x;
			}
			if (v.x > m_xmax) {
				m_xmax = v.x;
			}
			if (v.y < m_ymin) {
				m_ymin = v.y;
			}
			if (v.y > m_ymax) {
				m_ymax = v.y;
			}
		}
	};

	static public List<List<Vector2> > doLineDivPolygon (List<Vector2> vlist,Vector2 start,Vector2 end,Vector2 veryFarStartPoint) {
		//----calculate subPolygons
		List<List<Vector2> > subPolygonList=new List<List<Vector2> >();
		List<CsegEx> segList_outline=new List<CsegEx>();
		List<CvEx> divVExList=new List<CvEx>();
		int divCount = 0;
		for (int i = 0; i < vlist.Count; i++) {
			Vector2 v = vlist [i];
			int i_n=(i + 1) % vlist.Count;
			Vector2 vn = vlist [i_n];

			//line(start,end) div lineSeg(v,vn)
			Vector2 divPoint = getIntersectPoint_lineDivLineSeg (start, end, v, vn);
			float signedDisToDivLine = getSignedDisFromPointToLine (v,start,end);
			float signedDisToDivLine_n = getSignedDisFromPointToLine (vn,start,end);
			if (divPoint.x != float.MaxValue) {//divPoint exist
				divCount++;
				CvEx divVEx = new CvEx (divPoint,0,divCount);
				CsegEx seg1=new CsegEx();
				seg1.m_start = new CvEx (v,signedDisToDivLine,i);
				seg1.m_end = divVEx.copy ();
				seg1.m_signedDisFromMiddlePointToDivLine = (seg1.m_start.m_signedDisToDivLine + seg1.m_end.m_signedDisToDivLine) / 2;
				CsegEx seg2 = new CsegEx ();
				seg2.m_start = divVEx.copy ();
				seg2.m_end = new CvEx (vn,signedDisToDivLine_n,i_n);
				seg2.m_signedDisFromMiddlePointToDivLine = (seg2.m_start.m_signedDisToDivLine + seg2.m_end.m_signedDisToDivLine) / 2;
				divVExList.Add (divVEx);
				segList_outline.Add (seg1);
				segList_outline.Add (seg2);
			} else {
				CsegEx seg=new CsegEx();
				seg.m_start = new CvEx (v, signedDisToDivLine, i);
				seg.m_end = new CvEx (vn,signedDisToDivLine_n,i_n);
				seg.m_signedDisFromMiddlePointToDivLine = (seg.m_start.m_signedDisToDivLine + seg.m_end.m_signedDisToDivLine) / 2;
				segList_outline.Add(seg);
			}
		}//got segList_outline and divVExList
		if(divVExList.Count==0||divVExList.Count==1){
			return subPolygonList;
		}
		//----split segList_outline to rightSegList_outline and leftSegList_outline
		List<CsegEx> rightSegList_outline=getRightSegListFromSegList (segList_outline);
		List<CsegEx> leftSegList_outline = getLeftSegListFromSegList (segList_outline);
		//----get capSegList_shadow and capSegList_nonShadow

		List<CsegEx>	leftCapSegList = getLeftCapSegListFromDivVExList (divVExList, veryFarStartPoint);
		List<CsegEx>	rightCapSegList = getRevSegList (leftCapSegList);
	

		//----get rightSegList and leftSegList
		List<CsegEx> rightSegList = jointTwoSegList (rightCapSegList,rightSegList_outline);
		List<CsegEx> leftSegList = jointTwoSegList (leftCapSegList,leftSegList_outline);
		//----cluster segs
		List<List<CsegEx> > rightClusters=clusterSegs (rightSegList);
		List<List<CsegEx> > leftClusters=clusterSegs (leftSegList);
		//----clusters to subPolygons
		for(int i=0;i<rightClusters.Count;i++){
			List<CsegEx> cluster = rightClusters [i];
			List<Vector2> subPolygon=clusterToVlist(cluster);
			subPolygonList.Add (subPolygon);
		}
		for(int i=0;i<leftClusters.Count;i++){
			List<CsegEx> cluster = leftClusters[i];
			List<Vector2> subPolygon=clusterToVlist(cluster);
			subPolygonList.Add (subPolygon);
		}
		return subPolygonList;
	}
	static Vector2 getVeryFarStartPointForLineSeg(Vector2 lineSegStartPoint,Vector2 lineSegEndPoint,CboundBox boundBox)
	//so called "very far" means the result point will be out of boundBox
	{
		boundBox.extend (lineSegStartPoint);
		boundBox.extend (lineSegEndPoint);
		Vector2 size = boundBox.getSize ();
		float L=size.x + size.y;
		Vector2 veryFarStartPoint = lineSegStartPoint + (lineSegStartPoint - lineSegEndPoint).normalized * L;
		return veryFarStartPoint;
		
	}
	static List<CsegEx> getRightSegListFromSegList(List<CsegEx> segList){
		List<CsegEx> shadowSegList = new List<CsegEx> ();
		for (int i = 0; i < segList.Count; i++) {
			CsegEx seg = segList [i];
			if(seg.m_signedDisFromMiddlePointToDivLine<0){
				shadowSegList.Add (seg.copy());
			}
		}
		return shadowSegList;
	}
	static List<CsegEx> getLeftSegListFromSegList(List<CsegEx> segList){
		List<CsegEx> nonShadowSegList = new List<CsegEx> ();
		for (int i = 0; i < segList.Count; i++) {
			CsegEx seg = segList [i];
			if(seg.m_signedDisFromMiddlePointToDivLine>0){
				nonShadowSegList.Add (seg.copy());
			}
		}
		return nonShadowSegList;
	}
	static List<CsegEx> getLeftCapSegListFromDivVExList(List<CvEx> _divVExList,Vector2 divLineVeryFarStartPoint){
		List<CsegEx> capSegList = new List<CsegEx> ();
		if (_divVExList.Count == 0) {
			return capSegList;
		}
		List<CvEx> divVExList = copyVExList (_divVExList);
		divVExList.Sort ((x, y) => 
			{
				float value=Vector2.Distance (x.m_pos, divLineVeryFarStartPoint) - Vector2.Distance (y.m_pos, divLineVeryFarStartPoint);
				if(value==0){
					return 0;
				}else if(value>0){
					return 1;
				}else{
					return -1;
				}
			});
		for (int i = 0; i < divVExList.Count; i++) {
			if (i % 2 == 0) {
				if (i + 1 < divVExList.Count) {
					CvEx divVEx = divVExList [i];
					CvEx divVEx_n = divVExList [i + 1];
					CsegEx capSeg = new CsegEx ();
					capSeg.m_start = divVEx.copy ();
					capSeg.m_end = divVEx_n.copy ();
					capSeg.m_signedDisFromMiddlePointToDivLine = 0;
					capSegList.Add (capSeg);
				} else {
					//should not enter this branch
					Debug.LogError ("error!");
				}
			}
		}
		return capSegList;

	}
	static List<List<CsegEx> > clusterSegs(List<CsegEx> segs){
		List<List<CsegEx> > clusters = new List<List<CsegEx> > ();
		List<bool> isUsedList = new List<bool> ();
		for (int i = 0; i < segs.Count; i++) {
			isUsedList.Add (false);
		}
		List<CsegEx> t_cluster = new List<CsegEx> ();
		while (true) {

			if (t_cluster.Count == 0) {//t_cluster is empty
				int index = isUsedList.FindIndex (p=>p==false);
				if (index >=0) {//found
					t_cluster.Add (segs [index]);
					isUsedList [index] = true;
				} else {//not found
					//done
					break;
				
				}

			} else {//t_cluster is not empty
				CsegEx lastSegInCluster = t_cluster [t_cluster.Count - 1];
				//find lastSegInCluster's next seg or former seg
				bool isExtended = false;
				for (int i = 0; i < segs.Count; i++) {
					CsegEx seg = segs [i];
					if (isUsedList [i]==false){
						if (seg.m_start.m_ID==lastSegInCluster.m_end.m_ID&&seg.m_start.m_signedDisToDivLine==lastSegInCluster.m_end.m_signedDisToDivLine) {//next seg
							t_cluster.Add (seg);
							isUsedList [i] = true;
							isExtended = true;
							
						}else if(seg.m_end.m_ID== lastSegInCluster.m_start.m_ID&&seg.m_end.m_signedDisToDivLine==lastSegInCluster.m_start.m_signedDisToDivLine){//former seg
							t_cluster.Insert (0, seg);
							isUsedList [i] = true;
							isExtended = true;
							
						}

					}
				}
				if (isExtended == false) {//cluster is closed

					List<CsegEx> closedCluster = copySegList (t_cluster);
					clusters.Add (closedCluster);
					t_cluster.Clear ();
				}
			}
		}
		return clusters;
	}
	static List<Vector2>  clusterToVlist(List<CsegEx>  cluster){
		List<Vector2> vlist = new List<Vector2> ();
		for (int j = 0; j < cluster.Count; j++) {
			CsegEx seg = cluster [j];
			vlist.Add (seg.m_start.m_pos);
		}//got vlist
		return vlist;
		
	}
	static List<CsegEx> getRevSegList(List<CsegEx> segList){
		List<CsegEx> revSegList = new List<CsegEx> ();
		for (int i = 0; i < segList.Count; i++) {
			CsegEx seg = segList [i];
			CsegEx revSeg = new CsegEx ();
			revSeg.m_start = seg.m_end.copy ();
			revSeg.m_end = seg.m_start.copy ();
			revSeg.m_signedDisFromMiddlePointToDivLine = seg.m_signedDisFromMiddlePointToDivLine;
			revSegList.Add (revSeg);
		}
		return revSegList;
	
	}
	static List<CsegEx> jointTwoSegList(List<CsegEx> list1,List<CsegEx> list2){
		List<CsegEx> list = new List<CsegEx> ();
		for (int i = 0; i < list1.Count; i++) {
			CsegEx seg1 = list1 [i];
			CsegEx seg = seg1.copy ();
			list.Add (seg);
		}
		for (int i = 0; i < list2.Count; i++) {
			CsegEx seg2 = list2 [i];
			CsegEx seg = seg2.copy ();
			list.Add (seg);
		}
		return list;
	}
	static List<CsegEx> copySegList(List<CsegEx> list){
		List<CsegEx> _list = new List<CsegEx> ();
		for (int i = 0; i < list.Count; i++) {
			CsegEx seg = list [i];
			CsegEx _seg = seg.copy ();
			_list.Add (_seg);
		}
		return _list;
	}
	static List<CvEx> copyVExList(List<CvEx> list){
		List<CvEx> _list = new List<CvEx> ();
		for (int i = 0; i < list.Count; i++) {
			CvEx e = list [i];
			CvEx _e = e.copy ();
			_list.Add (_e);
		}
		return _list;
	}
	static public Vector2 getIntersectPoint_lineDivLineSeg(Vector2 P,Vector2 Q,Vector2 A,Vector2 B)
	//line PQ div lineSeg AB
	//return the intersect point
	//if there is no intersect point, return (float.maxValue,float.maxValue)
	{
		float signedDisPointAToLinePQ=getSignedDisFromPointToLine (A,P,Q);
		float signedDisPointBToLinePQ = getSignedDisFromPointToLine (B, P, Q);
		if (signedDisPointAToLinePQ * signedDisPointBToLinePQ <= 0) {//intersect 
			float disPointAToLinePQ = Mathf.Abs (signedDisPointAToLinePQ);
			float disPointBToLinePQ = Mathf.Abs (signedDisPointBToLinePQ);
			float kA = 0;
			if (disPointAToLinePQ + disPointBToLinePQ == 0) {
				kA = 0.5f;
			}else{
				kA = disPointAToLinePQ / (disPointAToLinePQ + disPointBToLinePQ);
			}
			Vector2 Mid = A + (B - A) * kA;
			return Mid;
		} else {
			return new Vector2 (float.MaxValue, float.MaxValue);
		}

	}
	static public float getSignedDisFromPointToLine(Vector2 P,Vector2 A,Vector2 B)
	//get signed distance from P to line AB
	{
		Vector2 lineDir = (B - A).normalized;
		Vector2 leftNormal = getLeftNormal (lineDir);
		Vector2 AP = P - A;
		float signedDis = Vector2.Dot (AP,leftNormal);
		return signedDis;

	}
	static public Vector2 getLeftNormal(Vector2 v){
		Vector2 leftNormal = new Vector2 (-v.y,v.x);
		leftNormal = leftNormal.normalized;
		return leftNormal;
	}

	static public Vector2 v3ToV2(Vector3 v){
		return new Vector2 (v.x,v.y);
	
	}

}
