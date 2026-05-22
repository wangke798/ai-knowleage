import React, { useState } from 'react';
import { 
  Home, 
  Folder, 
  Bot, 
  Calendar, 
  Hexagon, 
  ChevronDown, 
  ChevronUp,
  PanelLeftClose,
  ArrowRight,
} from 'lucide-react';
import { cn } from '@/lib/utils';

// 模拟的头像组件，用于 Chat bots 子菜单
const Avatar = ({ color, icon }) => (
  <div className={cn("w-5 h-5 rounded-full flex items-center justify-center text-[10px] text-white", color)}>
    {icon}
  </div>
);

const menuItems = [
  { id: 'home', label: 'Home', icon: Home },
  { id: 'insights', label: 'Insights', icon: Folder },
  { 
    id: 'chat-bots', 
    label: 'Chat bots', 
    icon: Bot,
    children: [
      { id: 'chatgpt4', label: 'ChatGPT-4', color: 'bg-blue-600', initial: 'G' },
      { id: 'poe', label: 'Poe', color: 'bg-purple-600', initial: 'P' },
      { id: 'openbot', label: 'OpenBot', color: 'bg-green-600', initial: 'O' },
      { id: 'luminarai', label: 'LuminarAI', color: 'bg-orange-500', initial: 'L' },
    ]
  },
  { id: 'schedule', label: 'Schedule', icon: Calendar },
  { id: 'nfts', label: 'NFTs', icon: Hexagon },
];

export const LightSidebar = () => {
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [activeMenu, setActiveMenu] = useState('insights');
  const [expandedMenus, setExpandedMenus] = useState({
    'chat-bots': true
  });

  return (
    <div 
      className={cn(
        "flex flex-col bg-white text-gray-500 rounded-3xl m-4 transition-all duration-300 relative shadow-sm border border-gray-200",
        isCollapsed ? "w-[72px]" : "w-64"
      )}
      style={{ height: 'calc(100vh - 32px)' }}
    >
      {/* 滚动菜单区 */}
      <div className="flex-1 overflow-y-auto overflow-x-hidden scrollbar-hide py-2 flex flex-col gap-1">
        {menuItems.map((item) => {
          const isActive = activeMenu === item.id;
          const isExpanded = expandedMenus[item.id];
          const hasChildren = item.children && item.children.length > 0;

          return (
            <div key={item.id} className="px-3">
              <div 
                className={cn(
                  "flex items-center rounded-xl cursor-pointer transition-colors duration-200 group",
                  isCollapsed ? "justify-center p-3" : "px-3 py-2.5",
                  isActive ? "bg-gray-100 text-gray-900" : "hover:bg-gray-50 hover:text-gray-900"
                )}
                onClick={() => {
                  setActiveMenu(item.id);
                  if (hasChildren && !isCollapsed) {
                    setExpandedMenus(prev => ({ ...prev, [item.id]: !prev[item.id] }));
                  }
                }}
              >
                <item.icon size={20} className={cn(isActive && "text-gray-900")} />
                {!isCollapsed && (
                  <>
                    <span className="ml-3 text-sm font-medium">{item.label}</span>
                    {hasChildren && (
                      <div className="ml-auto">
                        {isExpanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
                      </div>
                    )}
                  </>
                )}
              </div>

              {/* 展开的子菜单 */}
              {!isCollapsed && hasChildren && isExpanded && (
                <div className="mt-1 mb-2 flex flex-col gap-1 relative">
                  {item.children.map((child) => (
                    <div 
                      key={child.id} 
                      className="flex items-center gap-3 pr-3 pl-[44px] py-2 rounded-lg cursor-pointer hover:bg-gray-50 hover:text-gray-900 transition-colors relative group"
                    >
                      <span className="text-sm">{child.label}</span>
                      <div className="ml-auto">
                        <Avatar color={child.color} icon={child.initial} />
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* 底部折叠切换按钮 */}
      <div className="p-4 mt-auto">
        <div 
          className="flex items-center justify-center w-10 h-10 rounded-xl cursor-pointer hover:bg-gray-50 text-gray-400 hover:text-gray-900 transition-colors"
          onClick={() => setIsCollapsed(!isCollapsed)}
        >
          {isCollapsed ? <ArrowRight size={20} /> : <PanelLeftClose size={20} className="mr-auto ml-2" />}
        </div>
      </div>
    </div>
  );
};

export default LightSidebar;