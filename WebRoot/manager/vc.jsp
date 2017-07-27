<!--
Copyright (C) Huawei Technologies Co., LTD. 2006
 Author: Gong Lianyang
   Date: 2006-08-07
Version: V1.0
-->
<!
    DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
>
<%@page
    contentType = "image/jpeg"
    language    = "java"
    import      = "java.awt.*,java.awt.image.*,java.security.*,javax.imageio.*,java.util.Date,java.io.*,javax.imageio.stream.*"
%>
<%
    String FONT_NAMEs[] = {
        "Arial", "Courier", "Courier New", "Times New Roman"
        }; // 字体
           // 当字体可变时, 字体随机选取
           // 当字体固定时, 选第一个字体

    int VERIFY_CODE_TIMEOUT         = 5; // 验证码超时时间, 单位: 分钟, 当为 0 时表示永不超时
                                         //   备注: 验证码超时是通过 session 超时实现的
                                         //   即验证码超时 session 也会超时

    int VERIFY_CODE_MIN_SIZE        = 4; // 验证码的最小长度, 大于等于 4, 不超过 8
    int VERIFY_CODE_MAX_SIZE        = 4; // 验证码的最大长度, 大于等于 VERIFY_CODE_MIN_SIZE, 不超过 8
                                         //   当 VERIFY_CODE_MAX_SIZE == VERIFY_CODE_MIN_SIZE, 为固定长度
    int IS_VERIFY_CODE_SIZE_UNFIXED = 0; // 验证码的长度是否可变, 1: 可变, 0: 固定
                                         // 固定时, 验证码的长度为 VERIFY_CODE_MIN_SIZE

    int IS_CONTAIN_LOWERCASE        = 1; // 是否包含小写字母, 1: 包含, 0: 不包含
    int IS_CONTAIN_UPPERCASE        = 0; // 是否包含大写字母, 1: 包含, 0: 不包含
    int IS_CONTAIN_SPECIAL          = 0; // 是否包含特殊字符, 1: 包含, 0: 不包含

    int IS_BLACK_WRITE              = 0; // 是否为黑白图片, 1: 是, 0: 否
    int IS_SET_BACKGROUND           = 0; // 是否设置背景, 1: 设置, 0: 不设置
                                         // 当设置背景色时, 背景色随机变化
                                         // 当 IS_BLACK_WRITE = 1 时才有效
    int IS_SET_INTERFERON           = 1; // 是否设置干扰素, 1: 设置, 0: 不设置
                                         // 当设置背干扰素时并且不是黑白图片时, 干扰素的颜色随机变化
    int IS_FONT_NAME_UNFIXED        = 0; // 验证码字体是否可变, 1: 可变, 0: 固定
    int IS_FONT_SIZE_UNFIXED        = 0; // 验证码字体大小是否可变, 1: 可变, 0: 固定
    int IS_STR_POSITION_UNFIXED     = 0; // 验证码位置是否可变, 1: 可变, 0: 固定
%>
<%
    out.clear();

    //设置页面不缓存
    response.setHeader    ("Pragma",        "no-cache");
    response.setHeader    ("Cache-Control", "no-cache");
    response.setDateHeader("Expires",       0);

    // 创建随机对象
    SecureRandom g_oRandom = new SecureRandom();

    // 计算验证码的长度
    int g_iVerifyCodeSize = GetVerifyCodeSize(
        VERIFY_CODE_MIN_SIZE,
        VERIFY_CODE_MAX_SIZE,
        IS_VERIFY_CODE_SIZE_UNFIXED,
        g_oRandom
        );

    // 计算验证码图片的大小
    int FONT_SIZE = 20;
    int g_iWidth  = (FONT_SIZE - 2) * g_iVerifyCodeSize + 4;
    int g_iHeight = FONT_SIZE + 3;

    // 在内存中创建图象
    BufferedImage g_oImage = new BufferedImage(
        g_iWidth,
        g_iHeight,
        BufferedImage.TYPE_INT_RGB
        );

    // 获取图形上下文
    Graphics g_oGraphics = g_oImage.getGraphics();

    // 设定背景色
    if (IS_BLACK_WRITE != 0)
    {
        g_oGraphics.setColor(new Color(255, 255, 255));
    }
    else
    {
        if (IS_SET_BACKGROUND != 0)
        {
            g_oGraphics.setColor(GetRandColor(190, 255, g_oRandom));
        }
    }
    g_oGraphics.fillRect(0, 0, g_iWidth, g_iHeight);

    // 随机产生干扰线，使图象中的验证码不易被其它程序探测到
    if (IS_SET_INTERFERON != 0)
    {
        int l_iLines;

        int l_iPosX;
        int l_iPosY;
        int l_iWidth;
        int l_iHeight;

        int l_iTmp_0 = CreateRandom(99, g_oRandom);
        int l_iTmp_1 = (CreateRandom(3, g_oRandom) + 1) * 2;

        if (IS_BLACK_WRITE != 0)
        {
            if (l_iTmp_0 < 33)
            {
                l_iLines = g_iWidth / l_iTmp_1 + 1;
            }
            else
            {
                if (l_iTmp_0 > 66)
                {
                    l_iLines = g_iHeight / l_iTmp_1 + 1;
                }
                else
                {
                    l_iLines = 4 * g_iVerifyCodeSize - 1;
                }
            }

            g_oGraphics.setColor(new Color(0, 0, 0));
        }
        else
        {
            l_iLines = 39 * g_iVerifyCodeSize - 1;
        }

        for (int i = 0; i < l_iLines; i++)
        {
            if (IS_BLACK_WRITE != 0)
            {
                if (l_iTmp_0 < 33)
                {
                    l_iPosX   = i * l_iTmp_1 - 1;
                    l_iPosY   = 0;
                    l_iWidth  = 0;
                    l_iHeight = g_iHeight;
                }
                else
                {
                    if (l_iTmp_0 > 66)
                    {
                        l_iPosX   = 0;
                        l_iPosY   = i * l_iTmp_1 - 1;
                        l_iWidth  = g_iWidth;
                        l_iHeight = 0;
                    }
                    else
                    {
                        l_iPosX   = CreateRandom(g_iWidth,  g_oRandom);
                        l_iPosY   = CreateRandom(g_iHeight, g_oRandom);
                        l_iWidth  = CreateRandom(FONT_SIZE, g_oRandom);
                        l_iHeight = CreateRandom(g_iHeight, g_oRandom);
                    }
                }
            }
            else
            {
                l_iPosX   = CreateRandom(g_iWidth,  g_oRandom);
                l_iPosY   = CreateRandom(g_iHeight, g_oRandom);
                l_iWidth  = CreateRandom(FONT_SIZE, g_oRandom);
                l_iHeight = CreateRandom(g_iHeight, g_oRandom);
            }

            if (IS_BLACK_WRITE == 0)
            {
                g_oGraphics.setColor(GetRandColor(150, 200, g_oRandom));
            }

            g_oGraphics.drawLine(
                l_iPosX,
                l_iPosY,
                l_iPosX + l_iWidth,
                l_iPosY + l_iHeight
                );
        }
    }

    // 取随机产生的验证码
    int l_iContains = 1;

    if (IS_CONTAIN_LOWERCASE != 0)
    {
        l_iContains++;
    }

    if (IS_CONTAIN_UPPERCASE != 0)
    {
        l_iContains++;
    }

    if (IS_CONTAIN_SPECIAL != 0)
    {
        l_iContains++;
    }

    if (IS_BLACK_WRITE != 0)
    {
        g_oGraphics.setColor(new Color(0, 0, 0));
    }

    String g_stVerifyCode = "";

    for (int i = 0; i < g_iVerifyCodeSize; i++)
    {
        String l_stRand = "";

        int l_iKind = 0;

        if (l_iContains == 1)
        {
            l_stRand = String.valueOf(CreateRandom(10, g_oRandom));
        }
        else
        {
            l_iKind = CreateRandom(l_iContains, g_oRandom);

            switch (l_iKind)
            {
                case 3:
                {
                    l_stRand = GetOneSpecial(g_oRandom);
                    break;
                }
                case 2:
                {
                    if (IS_CONTAIN_UPPERCASE != 0)
                    {
                        l_stRand = GetOneUppercase(g_oRandom);
                    }
                    else
                    {
                        if (IS_CONTAIN_SPECIAL != 0)
                        {
                            l_stRand = GetOneSpecial(g_oRandom);
                        }
                    }

                    break;
                }
                case 1:
                {
                    if (IS_CONTAIN_LOWERCASE != 0)
                    {
                        l_stRand = GetOneLowercase(g_oRandom);
                    }
                    else
                    {
                        if (IS_CONTAIN_UPPERCASE != 0)
                        {
                            l_stRand = GetOneUppercase(g_oRandom);
                        }
                        else
                        {
                            if (IS_CONTAIN_SPECIAL != 0)
                            {
                                l_stRand = GetOneSpecial(g_oRandom);
                            }
                        }
                    }

                    break;
                }
                case 0:
                {
                    if ((IS_CONTAIN_UPPERCASE != 0)
                        || (IS_CONTAIN_LOWERCASE != 0)
                        )
                    {
                        l_stRand = String.valueOf(CreateRandom(9, g_oRandom) + 1);
                    }
                    else
                    {
                        l_stRand = String.valueOf(CreateRandom(10, g_oRandom));
                    }

                    break;
                }
            }
        }

        g_stVerifyCode += l_stRand;

        int l_iFontSize = FONT_SIZE;
        int l_iPos      = g_iHeight - FONT_SIZE / 4 - 1;
        int l_iStyle    = Font.BOLD;
        int l_iOffset   = g_iVerifyCodeSize;
        int l_iFontName = 0;

        if ((IS_FONT_SIZE_UNFIXED != 0)
            || (IS_STR_POSITION_UNFIXED != 0)
            )
        {
            if (IS_FONT_NAME_UNFIXED != 0)
            {
                l_iFontName = CreateRandom(FONT_NAMEs.length, g_oRandom);
            }

            l_iFontSize = FONT_SIZE;

            if (IS_FONT_SIZE_UNFIXED != 0)
            {
                int l_iTmpSize = 18;

                if (FONT_SIZE > 18)
                {
                    l_iTmpSize = FONT_SIZE - 18;
                    l_iFontSize = 18 + CreateRandom(l_iTmpSize, g_oRandom);
                }

                if (CreateRandom(100, g_oRandom) > 40)
                {
                    l_iStyle |= Font.ITALIC;
                }
            }

            if (IS_STR_POSITION_UNFIXED != 0)
            {
                int l_iType = 1;

                if (CreateRandom(2, g_oRandom) == 0)
                {
                    l_iType = -1;
                }

                l_iPos += (CreateRandom(3, g_oRandom) * l_iType);
            }

            l_iOffset = 4;
        }

        g_oGraphics.setFont(new Font(FONT_NAMEs[l_iFontName], l_iStyle, l_iFontSize));

        if (IS_BLACK_WRITE == 0)
        {
            g_oGraphics.setColor(GetRandColor(20, 200, g_oRandom)); // 20, 180
        }

        g_oGraphics.drawString(l_stRand, (FONT_SIZE - 2) * i + l_iOffset, l_iPos);
    }

    // 图象生效
    g_oGraphics.dispose();

    // 将验证码存入SESSION
    //session.setAttribute("ADMIN_VERIRY_CODE", g_stVerifyCode);
    Cookie cookie = new Cookie("ADMIN_VERIRY_CODE",g_stVerifyCode);
    cookie.setPath("/");
    response.addCookie(cookie);
    Date currTime = new Date();
    session.setAttribute("CREATE_TIEM", String.valueOf(currTime.getTime()));

    // 设定超时
    if (VERIFY_CODE_TIMEOUT > 0)
    {
        //session.setMaxInactiveInterval(60 * VERIFY_CODE_TIMEOUT);
        session.setAttribute("VERIFY_CODE_TIMEOUT",String.valueOf(60 * VERIFY_CODE_TIMEOUT));
    }

    //Begin modified by zm48772 at 2007-8-18 for NYFD13083
    // 输出图象到页面
    OutputStream os = null;
    ImageOutputStream stream = null;
    try
    {
        os =  response.getOutputStream();
        stream = ImageIO.createImageOutputStream(os);
        ImageIO.write(g_oImage, "jpeg", stream);
    }
    catch(IOException e)
    {
        e.printStackTrace();
    }
    finally
    {
        if(null != stream )
        {
           try
           {
               stream.close();
           }
           catch (IOException sEx)
           {
               sEx.printStackTrace();
           }
       }

       if(os != null )
       {
           try
           {
               os.close();
           }
           catch (IOException oEx)
           {
               oEx.printStackTrace();
           }
       }
   }

   //ImageIO.write(g_oImage, "PNG", response.getOutputStream());
   //End modified by zm48772 at 2007-8-18 for NYFD13083


    out.clear();
    out = pageContext.pushBody();
%>
<%!
    byte CreateRandom(int iSeed, SecureRandom oRandom)
    {
        byte[] l_pbtRand = new byte[1];

        oRandom.nextBytes(l_pbtRand);

        return (byte)Math.abs(l_pbtRand[0] % iSeed);
    }

    Color GetRandColor(int iFC, int iBC, SecureRandom oRandom)//给定范围获得随机颜色
    {
        if (iFC > 255)
        {
            iFC = 255;
        }

        if (iBC > 255)
        {
            iBC = 255;
        }

        int l_iTmp = iBC - iFC;

        if (l_iTmp == 0)
        {
            l_iTmp = 1;
        }

        int r = Math.abs((iFC + CreateRandom(l_iTmp, oRandom)) % 255);
        int g = Math.abs((iFC + CreateRandom(l_iTmp, oRandom)) % 255);
        int b = Math.abs((iFC + CreateRandom(l_iTmp, oRandom)) % 255);

        return new Color(r, g, b);
    }

    int GetVerifyCodeSize(int iMinSize, int iMaxSize, int iIsUnfixed, SecureRandom oRandom)
    {
        int MAX_SIZE = 8;

        int l_iMinSize = iMinSize;
        int l_iMaxSize = iMaxSize;

        if (l_iMinSize < 4)
        {
            l_iMinSize = 4;
        }

        if (l_iMinSize > MAX_SIZE)
        {
            l_iMinSize = MAX_SIZE;
            l_iMaxSize = MAX_SIZE;
        }

        if (l_iMaxSize > MAX_SIZE)
        {
            l_iMaxSize = MAX_SIZE;
        }

        if (l_iMaxSize < l_iMinSize)
        {
            l_iMaxSize = l_iMinSize;
        }

        if ((iIsUnfixed == 0)
            || (l_iMinSize == l_iMaxSize)
            )
        {
            return l_iMinSize;
        }

        return l_iMinSize + CreateRandom(l_iMaxSize - l_iMinSize + 1, oRandom);
    }

    String GetOneLowercase(SecureRandom oRandom)
    {
        int l_iTmp = CreateRandom(26, oRandom);
        char l_szStr[] = new char[2];

        l_szStr[0] = (char)(97 + l_iTmp);
        l_szStr[1] = (char)0;

        return String.valueOf(l_szStr[0]);
    }

    String GetOneUppercase(SecureRandom oRandom)
    {
        int l_iTmp = CreateRandom(26, oRandom);
        char l_szStr[] = new char[2];

        l_szStr[0] = (char)(65 + l_iTmp);
        l_szStr[1] = (char)0;

        return String.valueOf(l_szStr[0]);
    }

    String GetOneSpecial(SecureRandom oRandom)
    {
        char SPECIAL_CHAR[] = {'#','$','%','&','*','+','<','=','>','?','@','~'};

        int l_iTmp = CreateRandom(SPECIAL_CHAR.length, oRandom);
        char l_szStr[] = new char[2];

        l_szStr[0] = (char)SPECIAL_CHAR[l_iTmp];
        l_szStr[1] = (char)0;

        return String.valueOf(l_szStr[0]);
    }
%>
