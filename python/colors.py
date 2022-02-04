for ii in range(0, 9):
    for mm in range(0, 9):
        for jj in (3, 9):
             if mm==8 and jj==9:
                 continue
             for kk in (4, 10):
                for ll in range(0, 9):
                     print(f"{ii}, {jj}{mm}, {kk}{ll}: Oh, \x1b[{ii}m\x1b[{jj}{mm}m\x1b[{kk}{ll}mHey\x1b[m!!")
#     mm = 8
#     jj = 3
#     for kk in (4, 10):
#         for ll in range(0, 8):
#             print(f"{ii}, {jj}{mm}, {kk}{ll}: Oh, \x1b[{ii}m\x1b[{jj}{mm}m\x1b[{kk}{ll}mHey\x1b[m!!")
