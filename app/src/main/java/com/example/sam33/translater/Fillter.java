/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                             *
 *                         GNU General Public License                          *
 *                                                                             *
 *           本軟體使用GNU GPL授權，允許                                          *
 *           1. 以任何目的執行此程式的自由；                                       *
 *           2. 再發行複製件的自由；                                              *
 *           3. 改進此程式，並公開發布改進的自由                                    *
 *                                                                             *
 *           不過，相反地，所有GPL程式的演繹作品也要在GPL之下發布，促進自由軟體發展     *
 *                                                                             *
 *           本軟體作者：Sam33,國立中央大學資訊工程研究所                           *
 *                                                                             *
 *           本軟體在GNU GPL授權下使用星際譯王懶蟲字典的資料字典檔案                  *
 *                                                                             *
 *                                                                             *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */


package com.example.sam33.translater;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam33 on 16/2/2.
 */
class Fillter {

    protected String _Data;
    public Fillter()
    {

    }
    public Fillter(String Data)
    {
        _Data = Data;
    }
    public Fillter(Fillter f)
    {
        _Data = f.getData();
    }
    public String getData()
    {
        return _Data;
    }

}

class EnglishBlurryFillter extends Fillter {

    List<String> candidates;
    public List getCandidates()
    {
        return candidates;
    }
    public EnglishBlurryFillter(Fillter f)
    {
        super();
        candidates = new ArrayList<String>();
        _Data = f.getData();
        candidates.add(_Data);
        check_xs(_Data);
        check_xed(_Data);
        check_xing(_Data);
    }

    protected void check_xed(String source)
    {
        int length = source.length();
        char data[] = source.toCharArray();
        if(length>3) {
            if (data[length - 1] == 'd' && data[length - 2] == 'e') {
                candidates.add(source.substring(0, length - 1));
                candidates.add(source.substring(0, length - 2));
            }
        }
    }

    protected void check_xs(String source)
    {
        int length = source.length();
        char data[] = source.toCharArray();
        if(length>1) {
            if (data[length - 1] == 's')
                candidates.add(source.substring(0, length - 1));
        }
        if(length>2) {
            if (data[length - 1] == 's' && data[length - 2] == 'e')
                candidates.add(source.substring(0, length - 2));
        }

        if(length>3) {
            if (data[length - 1] == 's' && data[length - 2] == 'e' && data[length - 3] == 'i')
                candidates.add(source.substring(0, length - 3)+"y");
        }
    }

    protected void check_xing(String source)
    {
        int length = source.length();
        char data[] = source.toCharArray();
        if(length>3) {
            if (data[length - 1] == 'g' && data[length - 2] == 'n' && data[length - 3] == 'i')
                candidates.add(source.substring(0, length - 3));
        }
    }


}

class TrimFillter extends Fillter{

    public TrimFillter(Fillter f) {
        super();
        _Data = f.getData().trim();
    }
}

class LowerFillter extends Fillter{

    public LowerFillter(Fillter f) {
        super();
        _Data = f.getData().toLowerCase();
    }
}

class PunctuationMarkFillter extends Fillter{

    public PunctuationMarkFillter(Fillter f) {
        super();
        String tmp = f.getData();
        tmp = tmp.replace(",", "");
        tmp = tmp.replace(".", "");
        tmp = tmp.replace(";", "");
        tmp = tmp.replace("!", "");
        tmp = tmp.replace("?", "");
        tmp = tmp.replace("(", "");
        tmp = tmp.replace(")", "");
        tmp = tmp.replace("{", "");
        tmp = tmp.replace("}", "");
        tmp = tmp.replace("[", "");
        tmp = tmp.replace("]", "");
        tmp = tmp.replace("<", "");
        tmp = tmp.replace(">", "");
        tmp = tmp.replace("%", "");
        tmp = tmp.replace("\"", "");
        tmp = tmp.replace("^", "");
        _Data = tmp;
    }
}


