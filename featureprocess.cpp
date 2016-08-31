#include <cstdio>
#include <iostream>
#include <set>
using namespace std;
set<string> st;
set<string> :: iterator iter;
int main(){
    freopen("feature.txt","r",stdin);
    freopen("featureout.txt","w",stdout);
    string str;
    while(getline(cin,str)){
        st.insert(str);
    }
    for(iter = st.begin();iter!=st.end();iter++){
        cout<<(*iter)<<endl;
    }

    return 0;
}
